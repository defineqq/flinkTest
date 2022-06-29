/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datacenter.streaming.sql.connectors.elasticsearch.util;

import com.datacenter.streaming.sql.connectors.elasticsearch.ActionRequestFailureHandler;
import com.datacenter.streaming.sql.connectors.elasticsearch.RequestIndexer;
import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.util.ExceptionUtils;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.common.util.concurrent.EsRejectedExecutionException;
import org.elasticsearch.index.engine.VersionConflictEngineException;

/**
 * An {@link ActionRequestFailureHandler} that re-adds requests that failed due to temporary
 * {@link EsRejectedExecutionException}s (which means that Elasticsearch node queues are currently full),
 * and fails for all other failures.
 * @author flink
 */
@PublicEvolving
public class RetryRejectedExecutionFailureHandler implements ActionRequestFailureHandler {

	private static final long serialVersionUID = -7423562912824511906L;

	@Override
	public void onFailure(ActionRequest action, Throwable failure, int restStatusCode, RequestIndexer indexer) throws Throwable {
		// 如果是EsRejectedExecutionException异常, 重新index
		if (ExceptionUtils.findThrowable(failure, EsRejectedExecutionException.class).isPresent()) {
			indexer.add(action);
		// todo 这样做不规范
		//  (1) 版本冲突异常不应该放在RetryRejectedExecutionFailureHandler中
		//  (2) 版本冲突异常的处理方式欠佳。应该先获取最新版本，再去index，这样可以减少重试次数
		} else if (ExceptionUtils.findThrowable(failure, VersionConflictEngineException.class).isPresent()){
			indexer.add(action);
		}
		// 其他异常则抛出, Flink Job会重启
		else {
			throw failure;
		}
	}

}
