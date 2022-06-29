package com.datacenter.streaming.sql.connectors.redis.table;

import com.datacenter.streaming.sql.connectors.redis.RedisOptions;
import com.datacenter.streaming.sql.connectors.redis.RedisOutputFormat;
import com.datacenter.streaming.sql.connectors.redis.RedisSinkFunction;
import com.datacenter.streaming.sql.connectors.redis.RedisSinkOptions;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.api.constraints.UniqueConstraint;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.sink.DynamicTableSink;
import org.apache.flink.table.connector.sink.SinkFunctionProvider;
import org.apache.flink.types.RowKind;

import java.util.List;

import static org.apache.flink.util.Preconditions.checkArgument;
import static org.apache.flink.util.Preconditions.checkNotNull;
import static org.apache.flink.util.Preconditions.checkState;

/**
 * RedisDynamicTableSink
 *
 * @author wangpei
 */
public class RedisDynamicTableSink implements DynamicTableSink {

    private final RedisOptions redisOptions;
    private final RedisSinkOptions redisSinkOptions;
    private TableSchema physicalSchema;
    private int maxRetries;

    private List<String> keyFields;

    public RedisDynamicTableSink(RedisOptions redisOptions, RedisSinkOptions redisSinkOptions, TableSchema physicalSchema) {
        this.redisOptions = redisOptions;
        this.redisSinkOptions = redisSinkOptions;
        this.physicalSchema = physicalSchema;
        UniqueConstraint uniqueConstraint = physicalSchema.getPrimaryKey().orElse(null);
        checkNotNull(uniqueConstraint, "Redis sink must declare the primary key");
        this.keyFields = uniqueConstraint.getColumns();
        checkArgument(keyFields.size() == 1, "Redis sink can only have one primary key field");
        this.maxRetries = redisSinkOptions.getMaxRetries();
    }

    @Override
    public ChangelogMode getChangelogMode(ChangelogMode requestedMode) {
        validatePrimaryKey();
        return ChangelogMode.newBuilder()
                .addContainedKind(RowKind.INSERT)
                .addContainedKind(RowKind.DELETE)
                .addContainedKind(RowKind.UPDATE_AFTER)
                .build();
    }

    @Override
    public SinkRuntimeProvider getSinkRuntimeProvider(Context context) {
        RedisOutputFormat redisOutputFormat = new RedisOutputFormat(
                redisOptions.getHost(),
                redisOptions.getPort(),
                redisOptions.getDb(),
                redisOptions.getPassword(),
                redisOptions.getKeyType(),
                physicalSchema.getFieldNames(),
                physicalSchema.getFieldDataTypes(),
                keyFields.get(0),
                redisSinkOptions.getMaxRetries(),
                redisSinkOptions.getExpireSeconds()
        );

        RedisSinkFunction redisSinkFunction = new RedisSinkFunction(redisOutputFormat);
        return SinkFunctionProvider.of(redisSinkFunction);
        //return OutputFormatProvider.of(redisOutputFormat);
    }

    @Override
    public DynamicTableSink copy() {
        return new RedisDynamicTableSink(
                redisOptions,
                redisSinkOptions,
                physicalSchema);
    }

    @Override
    public String asSummaryString() {
        return null;
    }

    private void validatePrimaryKey() {
        checkState(keyFields != null, "please declare primary key for redis sink.");
    }
}
