package com.datacenter.streaming.sql.connectors.kudu.table;

import com.datacenter.streaming.sql.connectors.kudu.KuduLookupFunction;
import com.datacenter.streaming.sql.connectors.kudu.KuduLookupOptions;
import com.datacenter.streaming.sql.connectors.kudu.KuduOptions;
import lombok.AllArgsConstructor;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.connector.source.LookupTableSource;
import org.apache.flink.table.connector.source.TableFunctionProvider;
import org.apache.flink.table.types.DataType;
import org.apache.flink.util.Preconditions;

/**
 * KuduDynamicTableSource
 * @author wangpei
 */
@AllArgsConstructor
public class KuduDynamicTableSource implements LookupTableSource {

    private final KuduOptions kuduOptions;
    private final KuduLookupOptions kuduLookupOptions;
    private TableSchema physicalSchema;

    /**
     * 执行Loopup操作，从Kudu中Lookup数据
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

        // 返回的字段名
        String[] fieldNames = physicalSchema.getFieldNames();
        // 返回的字段类型
        DataType[] fieldTypes = physicalSchema.getFieldDataTypes();

        // 构建LookupFunction
        KuduLookupFunction kuduLookupFunction = new KuduLookupFunction(
                kuduOptions.getMaster(),
                kuduOptions.getTable(),
                fieldNames,
                fieldTypes,
                keyNames,
                kuduLookupOptions.getCacheMaxSize(),
                kuduLookupOptions.getCacheExpireMs(),
                kuduLookupOptions.getMaxRetryTimes());
        return TableFunctionProvider.of(kuduLookupFunction);
    }

    @Override
    public DynamicTableSource copy() {
        return new KuduDynamicTableSource(kuduOptions, kuduLookupOptions, physicalSchema);
    }

    @Override
    public String asSummaryString() {
        return null;
    }
}
