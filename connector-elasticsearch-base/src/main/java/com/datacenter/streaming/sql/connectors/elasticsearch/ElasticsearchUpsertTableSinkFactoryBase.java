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

package com.datacenter.streaming.sql.connectors.elasticsearch;

import com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator;
import com.datacenter.streaming.sql.connectors.elasticsearch.util.IgnoringFailureHandler;
import com.datacenter.streaming.sql.connectors.elasticsearch.util.NoOpFailureHandler;
import com.datacenter.streaming.sql.connectors.elasticsearch.util.RetryRejectedExecutionFailureHandler;
import org.apache.flink.annotation.Internal;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.api.ValidationException;
import org.apache.flink.table.descriptors.DescriptorProperties;
import org.apache.flink.table.descriptors.SchemaValidator;
import org.apache.flink.table.descriptors.StreamTableDescriptorValidator;
import org.apache.flink.table.factories.SerializationSchemaFactory;
import org.apache.flink.table.factories.StreamTableSinkFactory;
import org.apache.flink.table.factories.TableFactoryService;
import org.apache.flink.table.sinks.StreamTableSink;
import org.apache.flink.table.sinks.UpsertStreamTableSink;
import org.apache.flink.table.utils.TableSchemaUtils;
import org.apache.flink.types.Row;
import org.apache.flink.util.InstantiationUtil;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_BULK_FLUSH_BACKOFF_DELAY;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_BULK_FLUSH_BACKOFF_MAX_RETRIES;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_BULK_FLUSH_BACKOFF_TYPE;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_BULK_FLUSH_BACKOFF_TYPE_VALUE_CONSTANT;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_BULK_FLUSH_BACKOFF_TYPE_VALUE_DISABLED;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_BULK_FLUSH_BACKOFF_TYPE_VALUE_EXPONENTIAL;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_BULK_FLUSH_INTERVAL;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_BULK_FLUSH_MAX_ACTIONS;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_BULK_FLUSH_MAX_SIZE;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_CONNECTION_MAX_RETRY_TIMEOUT;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_CONNECTION_PATH_PREFIX;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_DOCUMENT_TYPE;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_FAILURE_HANDLER;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_FAILURE_HANDLER_CLASS;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_FAILURE_HANDLER_VALUE_CUSTOM;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_FAILURE_HANDLER_VALUE_FAIL;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_FAILURE_HANDLER_VALUE_IGNORE;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_FAILURE_HANDLER_VALUE_RETRY;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_FLUSH_ON_CHECKPOINT;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_HOSTS;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_HOSTS_HOSTNAME;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_HOSTS_PORT;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_HOSTS_PROTOCOL;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_INDEX;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_KEY_DELIMITER;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_KEY_NULL_LITERAL;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_TYPE_VALUE_ELASTICSEARCH;
import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.validateAndParseHostsString;
import static org.apache.flink.table.descriptors.ConnectorDescriptorValidator.CONNECTOR_PROPERTY_VERSION;
import static org.apache.flink.table.descriptors.ConnectorDescriptorValidator.CONNECTOR_TYPE;
import static org.apache.flink.table.descriptors.ConnectorDescriptorValidator.CONNECTOR_VERSION;
import static org.apache.flink.table.descriptors.DescriptorProperties.EXPR;
import static org.apache.flink.table.descriptors.DescriptorProperties.WATERMARK;
import static org.apache.flink.table.descriptors.DescriptorProperties.WATERMARK_ROWTIME;
import static org.apache.flink.table.descriptors.DescriptorProperties.WATERMARK_STRATEGY_DATA_TYPE;
import static org.apache.flink.table.descriptors.DescriptorProperties.WATERMARK_STRATEGY_EXPR;
import static org.apache.flink.table.descriptors.FormatDescriptorValidator.FORMAT;
import static org.apache.flink.table.descriptors.FormatDescriptorValidator.FORMAT_TYPE;
import static org.apache.flink.table.descriptors.Schema.SCHEMA;
import static org.apache.flink.table.descriptors.Schema.SCHEMA_DATA_TYPE;
import static org.apache.flink.table.descriptors.Schema.SCHEMA_NAME;
import static org.apache.flink.table.descriptors.Schema.SCHEMA_TYPE;
import static org.apache.flink.table.descriptors.StreamTableDescriptorValidator.UPDATE_MODE;
import static org.apache.flink.table.descriptors.StreamTableDescriptorValidator.UPDATE_MODE_VALUE_APPEND;

/**
 * Version-agnostic table factory for creating an {@link UpsertStreamTableSink} for Elasticsearch.
 * @author flink
 */
@Internal
public abstract class ElasticsearchUpsertTableSinkFactoryBase implements StreamTableSinkFactory<Tuple2<Boolean, Row>> {

	private static final String SUPPORTED_FORMAT_TYPE = "json";
	private static final XContentType SUPPORTED_CONTENT_TYPE = XContentType.JSON;
	private static final String DEFAULT_KEY_DELIMITER = "_";
	private static final String DEFAULT_KEY_NULL_LITERAL = "null";
	private static final String DEFAULT_FAILURE_HANDLER = CONNECTOR_FAILURE_HANDLER_VALUE_FAIL;

	@Override
	public Map<String, String> requiredContext() {
		final Map<String, String> context = new HashMap<>();
		context.put(CONNECTOR_TYPE, CONNECTOR_TYPE_VALUE_ELASTICSEARCH);
		context.put(CONNECTOR_VERSION, elasticsearchVersion());
		context.put(CONNECTOR_PROPERTY_VERSION, "1");
		return context;
	}

	@Override
	public List<String> supportedProperties() {
		final List<String> properties = new ArrayList<>();

		// streaming properties
		properties.add(UPDATE_MODE);

		// Elasticsearch
		properties.add(CONNECTOR_HOSTS);
		properties.add(CONNECTOR_HOSTS + ".#." + CONNECTOR_HOSTS_HOSTNAME);
		properties.add(CONNECTOR_HOSTS + ".#." + CONNECTOR_HOSTS_PORT);
		properties.add(CONNECTOR_HOSTS + ".#." + CONNECTOR_HOSTS_PROTOCOL);
		properties.add(CONNECTOR_INDEX);
		properties.add(CONNECTOR_DOCUMENT_TYPE);
		properties.add(CONNECTOR_KEY_DELIMITER);
		properties.add(CONNECTOR_KEY_NULL_LITERAL);
		properties.add(CONNECTOR_FAILURE_HANDLER);
		properties.add(CONNECTOR_FAILURE_HANDLER_CLASS);
		properties.add(CONNECTOR_FLUSH_ON_CHECKPOINT);
		properties.add(CONNECTOR_BULK_FLUSH_MAX_ACTIONS);
		properties.add(CONNECTOR_BULK_FLUSH_MAX_SIZE);
		properties.add(CONNECTOR_BULK_FLUSH_INTERVAL);
		properties.add(CONNECTOR_BULK_FLUSH_BACKOFF_TYPE);
		properties.add(CONNECTOR_BULK_FLUSH_BACKOFF_MAX_RETRIES);
		properties.add(CONNECTOR_BULK_FLUSH_BACKOFF_DELAY);
		properties.add(CONNECTOR_CONNECTION_MAX_RETRY_TIMEOUT);
		properties.add(CONNECTOR_CONNECTION_PATH_PREFIX);

		// schema
		properties.add(SCHEMA + ".#." + SCHEMA_DATA_TYPE);
		properties.add(SCHEMA + ".#." + SCHEMA_TYPE);
		properties.add(SCHEMA + ".#." + SCHEMA_NAME);
		// computed column
		properties.add(SCHEMA + ".#." + EXPR);

		// watermark
		properties.add(SCHEMA + "." + WATERMARK + ".#."  + WATERMARK_ROWTIME);
		properties.add(SCHEMA + "." + WATERMARK + ".#."  + WATERMARK_STRATEGY_EXPR);
		properties.add(SCHEMA + "." + WATERMARK + ".#."  + WATERMARK_STRATEGY_DATA_TYPE);

		// table constraint
		properties.add(SCHEMA + "." + DescriptorProperties.PRIMARY_KEY_NAME);
		properties.add(SCHEMA + "." + DescriptorProperties.PRIMARY_KEY_COLUMNS);

		// format wildcard
		properties.add(FORMAT + ".*");

		return properties;
	}

	@Override
	public StreamTableSink<Tuple2<Boolean, Row>> createStreamTableSink(Map<String, String> properties) {
		final DescriptorProperties descriptorProperties = getValidatedProperties(properties);

		return createElasticsearchUpsertTableSink(
			descriptorProperties.isValue(UPDATE_MODE, UPDATE_MODE_VALUE_APPEND),
			TableSchemaUtils.getPhysicalSchema(descriptorProperties.getTableSchema(SCHEMA)),
			getHosts(descriptorProperties),
			descriptorProperties.getString(CONNECTOR_INDEX),
			descriptorProperties.getString(CONNECTOR_DOCUMENT_TYPE),
			descriptorProperties.getOptionalString(CONNECTOR_KEY_DELIMITER).orElse(DEFAULT_KEY_DELIMITER),
			descriptorProperties.getOptionalString(CONNECTOR_KEY_NULL_LITERAL).orElse(DEFAULT_KEY_NULL_LITERAL),
			getSerializationSchema(properties),
			SUPPORTED_CONTENT_TYPE,
			getFailureHandler(descriptorProperties),
			getSinkOptions(descriptorProperties));
	}

	// --------------------------------------------------------------------------------------------
	// For version-specific factories
	// --------------------------------------------------------------------------------------------

	protected abstract String elasticsearchVersion();

	protected abstract ElasticsearchUpsertTableSinkBase createElasticsearchUpsertTableSink(
		boolean isAppendOnly,
		TableSchema schema,
		List<ElasticsearchUpsertTableSinkBase.Host> hosts,
		String index,
		String docType,
		String keyDelimiter,
		String keyNullLiteral,
		SerializationSchema<Row> serializationSchema,
		XContentType contentType,
		ActionRequestFailureHandler failureHandler,
		Map<ElasticsearchUpsertTableSinkBase.SinkOption, String> sinkOptions);

	// --------------------------------------------------------------------------------------------
	// Helper methods
	// --------------------------------------------------------------------------------------------

	private DescriptorProperties getValidatedProperties(Map<String, String> properties) {
		final DescriptorProperties descriptorProperties = new DescriptorProperties(true);
		descriptorProperties.putProperties(properties);

		new StreamTableDescriptorValidator(true, false, true).validate(descriptorProperties);
		new SchemaValidator(true, false, false).validate(descriptorProperties);
		new ElasticsearchValidator().validate(descriptorProperties);

		return descriptorProperties;
	}

	private List<ElasticsearchUpsertTableSinkBase.Host> getHosts(DescriptorProperties descriptorProperties) {
		if (descriptorProperties.containsKey(CONNECTOR_HOSTS)) {
			return validateAndParseHostsString(descriptorProperties);
		} else {
			final List<Map<String, String>> hosts = descriptorProperties.getFixedIndexedProperties(
				CONNECTOR_HOSTS,
				Arrays.asList(CONNECTOR_HOSTS_HOSTNAME, CONNECTOR_HOSTS_PORT, CONNECTOR_HOSTS_PROTOCOL));
			return hosts.stream()
				.map(host -> new ElasticsearchUpsertTableSinkBase.Host(
					descriptorProperties.getString(host.get(CONNECTOR_HOSTS_HOSTNAME)),
					descriptorProperties.getInt(host.get(CONNECTOR_HOSTS_PORT)),
					descriptorProperties.getString(host.get(CONNECTOR_HOSTS_PROTOCOL))))
				.collect(Collectors.toList());
		}
	}

	private SerializationSchema<Row> getSerializationSchema(Map<String, String> properties) {
		final String formatType = properties.get(FORMAT_TYPE);
		// we could have added this check to the table factory context
		// but this approach allows to throw more helpful error messages
		// if the supported format has not been added
		if (formatType == null || !formatType.equals(SUPPORTED_FORMAT_TYPE)) {
			throw new ValidationException(
				"The Elasticsearch sink requires a '" + SUPPORTED_FORMAT_TYPE + "' format.");
		}

		@SuppressWarnings("unchecked")
		final SerializationSchemaFactory<Row> formatFactory = TableFactoryService.find(
			SerializationSchemaFactory.class,
			properties,
			this.getClass().getClassLoader());
		return formatFactory.createSerializationSchema(properties);
	}

	private ActionRequestFailureHandler getFailureHandler(DescriptorProperties descriptorProperties) {
		final String failureHandler = descriptorProperties
			.getOptionalString(CONNECTOR_FAILURE_HANDLER)
			.orElse(DEFAULT_FAILURE_HANDLER);
		switch (failureHandler) {
			case CONNECTOR_FAILURE_HANDLER_VALUE_FAIL:
				return new NoOpFailureHandler();
			case CONNECTOR_FAILURE_HANDLER_VALUE_IGNORE:
				return new IgnoringFailureHandler();
			case CONNECTOR_FAILURE_HANDLER_VALUE_RETRY:
				return new RetryRejectedExecutionFailureHandler();
			case CONNECTOR_FAILURE_HANDLER_VALUE_CUSTOM:
				final Class<? extends ActionRequestFailureHandler> clazz = descriptorProperties
					.getClass(CONNECTOR_FAILURE_HANDLER_CLASS, ActionRequestFailureHandler.class);
				return InstantiationUtil.instantiate(clazz);
			default:
				throw new IllegalArgumentException("Unknown failure handler.");
		}
	}

	private Map<ElasticsearchUpsertTableSinkBase.SinkOption, String> getSinkOptions(DescriptorProperties descriptorProperties) {
		final Map<ElasticsearchUpsertTableSinkBase.SinkOption, String> options = new HashMap<>();

		descriptorProperties.getOptionalBoolean(CONNECTOR_FLUSH_ON_CHECKPOINT)
			.ifPresent(v -> options.put(ElasticsearchUpsertTableSinkBase.SinkOption.DISABLE_FLUSH_ON_CHECKPOINT, String.valueOf(!v)));

		mapSinkOption(descriptorProperties, options, CONNECTOR_BULK_FLUSH_MAX_ACTIONS, ElasticsearchUpsertTableSinkBase.SinkOption.BULK_FLUSH_MAX_ACTIONS);
		mapSinkOption(descriptorProperties, options, CONNECTOR_BULK_FLUSH_MAX_SIZE, ElasticsearchUpsertTableSinkBase.SinkOption.BULK_FLUSH_MAX_SIZE);
		mapSinkOption(descriptorProperties, options, CONNECTOR_BULK_FLUSH_INTERVAL, ElasticsearchUpsertTableSinkBase.SinkOption.BULK_FLUSH_INTERVAL);

		descriptorProperties.getOptionalString(CONNECTOR_BULK_FLUSH_BACKOFF_TYPE)
			.ifPresent(v -> {
				options.put(
					ElasticsearchUpsertTableSinkBase.SinkOption.BULK_FLUSH_BACKOFF_ENABLED,
					String.valueOf(!v.equals(CONNECTOR_BULK_FLUSH_BACKOFF_TYPE_VALUE_DISABLED)));
				switch (v) {
					case CONNECTOR_BULK_FLUSH_BACKOFF_TYPE_VALUE_CONSTANT:
						options.put(
							ElasticsearchUpsertTableSinkBase.SinkOption.BULK_FLUSH_BACKOFF_TYPE,
							ElasticsearchSinkBase.FlushBackoffType.CONSTANT.toString());
						break;
					case CONNECTOR_BULK_FLUSH_BACKOFF_TYPE_VALUE_EXPONENTIAL:
						options.put(
							ElasticsearchUpsertTableSinkBase.SinkOption.BULK_FLUSH_BACKOFF_TYPE,
							ElasticsearchSinkBase.FlushBackoffType.EXPONENTIAL.toString());
						break;
					default:
						throw new IllegalArgumentException("Unknown backoff type.");
				}
			});

		mapSinkOption(descriptorProperties, options, CONNECTOR_BULK_FLUSH_BACKOFF_MAX_RETRIES, ElasticsearchUpsertTableSinkBase.SinkOption.BULK_FLUSH_BACKOFF_RETRIES);
		mapSinkOption(descriptorProperties, options, CONNECTOR_BULK_FLUSH_BACKOFF_DELAY, ElasticsearchUpsertTableSinkBase.SinkOption.BULK_FLUSH_BACKOFF_DELAY);
		mapSinkOption(descriptorProperties, options, CONNECTOR_CONNECTION_MAX_RETRY_TIMEOUT, ElasticsearchUpsertTableSinkBase.SinkOption.REST_MAX_RETRY_TIMEOUT);
		mapSinkOption(descriptorProperties, options, CONNECTOR_CONNECTION_PATH_PREFIX, ElasticsearchUpsertTableSinkBase.SinkOption.REST_PATH_PREFIX);

		return options;
	}

	private void mapSinkOption(
			DescriptorProperties descriptorProperties,
			Map<ElasticsearchUpsertTableSinkBase.SinkOption, String> options,
			String fromKey,
			ElasticsearchUpsertTableSinkBase.SinkOption toKey) {
		descriptorProperties.getOptionalString(fromKey).ifPresent(v -> options.put(toKey, v));
	}
}
