/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datacenter.streaming.sql.connectors.elasticsearch7.table;

import com.datacenter.streaming.sql.connectors.elasticsearch.table.IndexGeneratorFactory;
import com.datacenter.streaming.sql.connectors.elasticsearch.table.KeyExtractor;
import com.datacenter.streaming.sql.connectors.elasticsearch.table.RequestFactory;
import com.datacenter.streaming.sql.connectors.elasticsearch.table.RowElasticsearchSinkFunction;
import com.datacenter.streaming.sql.connectors.elasticsearch7.ElasticsearchSink;
import com.datacenter.streaming.sql.connectors.elasticsearch7.RestClientFactory;
import org.apache.flink.annotation.Internal;
import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.format.EncodingFormat;
import org.apache.flink.table.connector.sink.DynamicTableSink;
import org.apache.flink.table.connector.sink.SinkFunctionProvider;
import org.apache.flink.table.data.RowData;
import org.apache.flink.types.RowKind;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.common.xcontent.XContentType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * A {@link DynamicTableSink} that describes how to create a {@link ElasticsearchSink} from a logical
 * description.
 */
@Internal
final class Elasticsearch7DynamicSink implements DynamicTableSink {
	@VisibleForTesting
	static final Elasticsearch7RequestFactory REQUEST_FACTORY = new Elasticsearch7DynamicSink.Elasticsearch7RequestFactory();

	private final EncodingFormat<SerializationSchema<RowData>> format;
	private final TableSchema schema;
	private final Elasticsearch7Configuration config;

	public Elasticsearch7DynamicSink(
			EncodingFormat<SerializationSchema<RowData>> format,
			Elasticsearch7Configuration config,
			TableSchema schema) {
		this(format, config, schema, (ElasticsearchSink.Builder::new));
	}

	//--------------------------------------------------------------
	// Hack to make configuration testing possible.
	//
	// The code in this block should never be used outside of tests.
	// Having a way to inject a builder we can assert the builder in
	// the test. We can not assert everything though, e.g. it is not
	// possible to assert flushing on checkpoint, as it is configured
	// on the sink itself.
	//--------------------------------------------------------------

	private final ElasticSearchBuilderProvider builderProvider;

	@FunctionalInterface
	interface ElasticSearchBuilderProvider {
		ElasticsearchSink.Builder<RowData> createBuilder(
                List<HttpHost> httpHosts,
                RowElasticsearchSinkFunction upsertSinkFunction);
	}

	Elasticsearch7DynamicSink(
			EncodingFormat<SerializationSchema<RowData>> format,
			Elasticsearch7Configuration config,
			TableSchema schema,
			ElasticSearchBuilderProvider builderProvider) {
		this.format = format;
		this.schema = schema;
		this.config = config;
		this.builderProvider = builderProvider;
	}

	//--------------------------------------------------------------
	// End of hack to make configuration testing possible
	//--------------------------------------------------------------

	@Override
	public ChangelogMode getChangelogMode(ChangelogMode requestedMode) {
		ChangelogMode.Builder builder = ChangelogMode.newBuilder();
		for (RowKind kind : requestedMode.getContainedKinds()) {
			if (kind != RowKind.UPDATE_BEFORE) {
				builder.addContainedKind(kind);
			}
		}
		return builder.build();
	}

	@Override
	public SinkFunctionProvider getSinkRuntimeProvider(Context context) {
		return () -> {
			SerializationSchema<RowData> format = this.format.createRuntimeEncoder(context, schema.toRowDataType());

			final RowElasticsearchSinkFunction upsertFunction =
				new RowElasticsearchSinkFunction(
					IndexGeneratorFactory.createIndexGenerator(config.getIndex(), schema),
					null, // this is deprecated in es 7+
					format,
					XContentType.JSON,
					REQUEST_FACTORY,
					KeyExtractor.createKeyExtractor(schema, config.getKeyDelimiter())
				);

			final ElasticsearchSink.Builder<RowData> builder = builderProvider.createBuilder(
				config.getHosts(),
				upsertFunction);

			builder.setFailureHandler(config.getFailureHandler());
			builder.setBulkFlushMaxActions(config.getBulkFlushMaxActions());
			builder.setBulkFlushMaxSizeMb((int) (config.getBulkFlushMaxByteSize() >> 20));
			builder.setBulkFlushInterval(config.getBulkFlushInterval());
			builder.setBulkFlushBackoff(config.isBulkFlushBackoffEnabled());
			config.getBulkFlushBackoffType().ifPresent(builder::setBulkFlushBackoffType);
			config.getBulkFlushBackoffRetries().ifPresent(builder::setBulkFlushBackoffRetries);
			config.getBulkFlushBackoffDelay().ifPresent(builder::setBulkFlushBackoffDelay);

			// we must overwrite the default factory which is defined with a lambda because of a bug
			// in shading lambda serialization shading see FLINK-18006
			builder.setRestClientFactory(new DefaultRestClientFactory(
					config.getPathPrefix().orElse(null),
					config.getUsername().orElse(null),
					config.getPassword().orElse(null)
					));

			final ElasticsearchSink<RowData> sink = builder.build();

			if (config.isDisableFlushOnCheckpoint()) {
				sink.disableFlushOnCheckpoint();
			}

			return sink;
		};
	}

	@Override
	public DynamicTableSink copy() {
		return this;
	}

	@Override
	public String asSummaryString() {
		return "Elasticsearch7";
	}

	/**
	 * Serializable {@link RestClientFactory} used by the sink.
	 */
	@VisibleForTesting
	static class DefaultRestClientFactory implements RestClientFactory {

		private final String pathPrefix;
		private String username;
		private String password;

		public DefaultRestClientFactory(@Nullable String pathPrefix, String username, String password) {
			this.pathPrefix = pathPrefix;
			if (username != null && password != null) {
				this.username = username;
				this.password = password;
			}
		}

		@Override
		public void configureRestClientBuilder(RestClientBuilder restClientBuilder) {
			if (pathPrefix != null) {
				restClientBuilder.setPathPrefix(pathPrefix);
			}

			// 添加权限认证
			if (username != null && password != null) {
				BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
				basicCredentialsProvider.setCredentials(
						AuthScope.ANY,
						new UsernamePasswordCredentials(username, password)
				);
				restClientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
					@Override
					public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
						return httpAsyncClientBuilder.setDefaultCredentialsProvider(basicCredentialsProvider);
					}
				});
			}
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			DefaultRestClientFactory that = (DefaultRestClientFactory) o;
			return Objects.equals(pathPrefix, that.pathPrefix);
		}

		@Override
		public int hashCode() {
			return Objects.hash(pathPrefix);
		}
	}

	/**
	 * Version-specific creation of {@link org.elasticsearch.action.ActionRequest}s used by the sink.
	 */
	private static class Elasticsearch7RequestFactory implements RequestFactory {
		@Override
		public UpdateRequest createUpdateRequest(
				String index,
				String docType,
				String key,
				XContentType contentType,
				byte[] document) {
			return new UpdateRequest(index, key)
				.doc(document, contentType)
				.upsert(document, contentType);
		}

		@Override
		public IndexRequest createIndexRequest(
				String index,
				String docType,
				String key,
				XContentType contentType,
				byte[] document) {
			return new IndexRequest(index)
				.id(key)
				.source(document, contentType);
		}

		@Override
		public DeleteRequest createDeleteRequest(String index, String docType, String key) {
			return new DeleteRequest(index, key);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Elasticsearch7DynamicSink that = (Elasticsearch7DynamicSink) o;
		return Objects.equals(format, that.format) &&
			Objects.equals(schema, that.schema) &&
			Objects.equals(config, that.config) &&
			Objects.equals(builderProvider, that.builderProvider);
	}

	@Override
	public int hashCode() {
		return Objects.hash(format, schema, config, builderProvider);
	}
}
