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

package com.datacenter.streaming.sql.connectors.elasticsearch6;

import com.datacenter.streaming.sql.connectors.elasticsearch.ActionRequestFailureHandler;
import com.datacenter.streaming.sql.connectors.elasticsearch.ElasticsearchUpsertTableSinkBase;
import com.datacenter.streaming.sql.connectors.elasticsearch.ElasticsearchUpsertTableSinkFactoryBase;
import org.apache.flink.annotation.Internal;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.sinks.UpsertStreamTableSink;
import org.apache.flink.types.Row;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.List;
import java.util.Map;

import static com.datacenter.streaming.sql.connectors.elasticsearch.descriptors.ElasticsearchValidator.CONNECTOR_VERSION_VALUE_6;


/**
 * Table factory for creating an {@link UpsertStreamTableSink} for Elasticsearch 6.
 * @author flink
 */
@Internal
public class Elasticsearch6UpsertTableSinkFactory extends ElasticsearchUpsertTableSinkFactoryBase {

	@Override
	protected String elasticsearchVersion() {
		return CONNECTOR_VERSION_VALUE_6;
	}

	@Override
	protected ElasticsearchUpsertTableSinkBase createElasticsearchUpsertTableSink(
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
			Map<ElasticsearchUpsertTableSinkBase.SinkOption, String> sinkOptions) {

		return new Elasticsearch6UpsertTableSink(
			isAppendOnly,
			schema,
			hosts,
			index,
			docType,
			keyDelimiter,
			keyNullLiteral,
			serializationSchema,
			contentType,
			failureHandler,
			sinkOptions);
	}
}
