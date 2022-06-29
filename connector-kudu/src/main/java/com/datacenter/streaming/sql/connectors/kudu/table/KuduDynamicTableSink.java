package com.datacenter.streaming.sql.connectors.kudu.table;

import com.datacenter.streaming.sql.connectors.kudu.KuduOptions;
import com.datacenter.streaming.sql.connectors.kudu.KuduOutputFormat;
import com.datacenter.streaming.sql.connectors.kudu.KuduSinkOptions;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.api.constraints.UniqueConstraint;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.sink.DynamicTableSink;
import org.apache.flink.table.connector.sink.OutputFormatProvider;
import org.apache.flink.types.RowKind;

import java.util.List;

import static org.apache.flink.util.Preconditions.checkState;

/**
 * KuduDynamicTableSink
 * @author wangpei
 */
public class KuduDynamicTableSink implements DynamicTableSink {

    private final KuduOptions kuduOptions;
    private final KuduSinkOptions kuduSinkOptions;
    private TableSchema physicalSchema;
    private int bufferFlushInterval;
    private int maxRetries;

    private List<String> keyFields;

    public KuduDynamicTableSink(KuduOptions kuduOptions, KuduSinkOptions kuduSinkOptions, TableSchema physicalSchema) {
        this.kuduOptions = kuduOptions;
        this.kuduSinkOptions = kuduSinkOptions;
        this.physicalSchema = physicalSchema;
        UniqueConstraint uniqueConstraint = physicalSchema.getPrimaryKey().orElse(null);
        if (uniqueConstraint != null) {
            this.keyFields = uniqueConstraint.getColumns();
        }
        this.bufferFlushInterval = (int) kuduSinkOptions.getBatchIntervalMs();
        this.maxRetries = kuduSinkOptions.getMaxRetries();
    }

    @Override
    public ChangelogMode getChangelogMode(ChangelogMode requestedMode) {
        validatePrimaryKey(requestedMode);
        return ChangelogMode.newBuilder()
                .addContainedKind(RowKind.INSERT)
                .addContainedKind(RowKind.UPDATE_AFTER)
                .build();
    }

    @Override
    public SinkRuntimeProvider getSinkRuntimeProvider(Context context) {
        KuduOutputFormat kuduOutputFormat = new KuduOutputFormat(
                kuduOptions.getMaster(),
                kuduOptions.getTable(),
                physicalSchema.getFieldNames(),
                physicalSchema.getFieldDataTypes(),
                bufferFlushInterval,
                maxRetries
        );

        return OutputFormatProvider.of(kuduOutputFormat);
    }

    @Override
    public DynamicTableSink copy() {
        return new KuduDynamicTableSink(
                kuduOptions,
                kuduSinkOptions,
                physicalSchema);
    }

    @Override
    public String asSummaryString() {
        return null;
    }

    private void validatePrimaryKey(ChangelogMode requestedMode) {
        checkState(ChangelogMode.insertOnly().equals(requestedMode) || keyFields == null,
                "please declare primary key for sink table when query contains update/delete record.");
    }
}
