package com.datacenter.streaming.sql.connectors.redis.table;

import com.datacenter.streaming.sql.connectors.redis.RedisLookupFunction;
import com.datacenter.streaming.sql.connectors.redis.RedisLookupOptions;
import com.datacenter.streaming.sql.connectors.redis.RedisOptions;
import lombok.AllArgsConstructor;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.connector.source.LookupTableSource;
import org.apache.flink.table.connector.source.TableFunctionProvider;
import org.apache.flink.table.types.DataType;
import org.apache.flink.util.Preconditions;

import static org.apache.flink.util.Preconditions.checkArgument;

/**
 * RedisDynamicTableSource
 *
 * @author wangpei
 */
@AllArgsConstructor
public class RedisDynamicTableSource implements LookupTableSource {

    private final RedisOptions redisOptions;
    private final RedisLookupOptions redisLookupOptions;
    private TableSchema physicalSchema;

    /**
     * 执行Loopup操作，从Kudu中Lookup数据
     *
     * @param context
     * @return
     */
    @Override
    public LookupRuntimeProvider getLookupRuntimeProvider(LookupContext context) {

        // 查找的键名
        String[] keyNames = new String[context.getKeys().length];
        for (int i = 0; i < keyNames.length; i++) {
            int[] innerKeyArr = context.getKeys()[i];
            Preconditions.checkArgument(innerKeyArr.length == 1, "Kudu only support non-nested look up keys");
            keyNames[i] = physicalSchema.getFieldNames()[innerKeyArr[0]];
        }
        checkArgument(keyNames.length == 1, "Redis dim can and must only have one lookup key field");

        // 返回的字段名
        String[] fieldNames = physicalSchema.getFieldNames();
        // 返回的字段类型
        DataType[] fieldTypes = physicalSchema.getFieldDataTypes();

        // 构建LookupFunction
        RedisLookupFunction redisLookupFunction = new RedisLookupFunction(
                redisOptions.getHost(),
                redisOptions.getPort(),
                redisOptions.getDb(),
                redisOptions.getPassword(),
                redisOptions.getKeyType(),
                fieldNames,
                fieldTypes,
                keyNames[0],
                redisLookupOptions.getCacheMaxSize(),
                redisLookupOptions.getCacheExpireMs(),
                redisLookupOptions.getMaxRetryTimes());
        return TableFunctionProvider.of(redisLookupFunction);
    }

    @Override
    public DynamicTableSource copy() {
        return new RedisDynamicTableSource(redisOptions, redisLookupOptions, physicalSchema);
    }

    @Override
    public String asSummaryString() {
        return null;
    }
}
