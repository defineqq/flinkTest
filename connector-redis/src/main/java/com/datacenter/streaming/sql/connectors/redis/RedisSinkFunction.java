package com.datacenter.streaming.sql.connectors.redis;

import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.table.data.RowData;

/**
 * RedisSinkFunction
 * @author wangpei
 */
public class RedisSinkFunction extends RichSinkFunction<RowData> implements CheckpointedFunction {
    final RedisOutputFormat redisOutputFormat;

    public RedisSinkFunction(RedisOutputFormat redisOutputFormat) {
        this.redisOutputFormat = redisOutputFormat;
    }

    @Override
    public void snapshotState(FunctionSnapshotContext functionSnapshotContext) throws Exception {
    }

    @Override
    public void initializeState(FunctionInitializationContext functionInitializationContext) throws Exception {

    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        RuntimeContext ctx = getRuntimeContext();
        redisOutputFormat.setRuntimeContext(ctx);
        redisOutputFormat.open(ctx.getIndexOfThisSubtask(), ctx.getNumberOfParallelSubtasks());
    }

    @Override
    public void invoke(RowData value, Context context) throws Exception {
        redisOutputFormat.writeRecord(value);
    }

    @Override
    public void close() throws Exception {
        redisOutputFormat.close();
        super.close();
    }
}
