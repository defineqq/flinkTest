package com.datacenter.streaming.sql.connectors.kudu;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.types.DataType;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduSession;
import org.apache.kudu.client.KuduTable;
import org.apache.kudu.client.PartialRow;
import org.apache.kudu.client.SessionConfiguration;
import org.apache.kudu.client.Upsert;

import java.io.IOException;

/**
 * KuduOutputFormat
 *
 * @author wangpei
 */
@Slf4j
public class KuduOutputFormat implements OutputFormat<RowData> {

    private String master;
    private String table;
    private final String[] fieldNames;
    private final DataType[] fieldTypes;

    private KuduClient kuduClient;
    private KuduTable kuduTable;
    private KuduSession kuduSession;

    private int bufferFlushInterval;
    private int maxRetries;

    public KuduOutputFormat(String master, String table, String[] fieldNames, DataType[] fieldTypes, int bufferFlushInterval, int maxRetries) {
        this.master = master;
        this.table = table;
        this.fieldNames = fieldNames;
        this.fieldTypes = fieldTypes;
        this.bufferFlushInterval = bufferFlushInterval;
        this.maxRetries = maxRetries;
    }

    @Override
    public void configure(Configuration configuration) {
    }

    @Override
    public void open(int taskNumber, int numTasks) {
        obtainConn();
    }

    @Override
    public void writeRecord(RowData rowData) throws IOException {
        obtainConn();
        Upsert upsert = kuduTable.newUpsert();
        PartialRow partialRow = upsert.getRow();
        for (int i = 0; i < fieldNames.length; i++) {
            DataType fieldType = fieldTypes[i];
            boolean nullAt = rowData.isNullAt(i);
            if (!nullAt) {
                String toUpperCase = fieldType.getConversionClass().getSimpleName().toUpperCase();
                if (toUpperCase.equals("STRING")) {
                    StringData value = rowData.getString(i);
                    partialRow.addString(fieldNames[i], value.toString());
                } else if (toUpperCase.equals("LONG")) {
                    long value = rowData.getLong(i);
                    partialRow.addLong(fieldNames[i], rowData.getLong(i));
                } else if (toUpperCase.equals("DOUBLE")) {
                    double value = rowData.getDouble(i);
                    partialRow.addDouble(fieldNames[i], value);
                }
            }
        }
        kuduSession.apply(upsert);
    }

    @Override
    public void close() throws IOException {
        releaseConn();
    }

    /**
     * 获取连接
     */
    private void obtainConn() {
        try {
            if (kuduClient == null) {
                kuduClient = new KuduClient.KuduClientBuilder(master).build();
            }

            if (kuduSession == null || kuduSession.isClosed()) {
                kuduSession = kuduClient.newSession();
                kuduSession.setFlushMode(SessionConfiguration.FlushMode.AUTO_FLUSH_BACKGROUND);
                kuduSession.setFlushInterval(bufferFlushInterval);
            }

            if (kuduTable == null) {
                kuduTable = kuduClient.openTable(table);
            }

        } catch (Exception ex) {
            throw new RuntimeException("获取连接...", ex);
        }
    }

    /**
     * 释放连接
     */
    private void releaseConn() {
        try {
            //if (kuduSession != null && !kuduSession.isClosed()){
            //    kuduSession.close();
            //}

            if (kuduClient != null) {
                kuduClient.close();
            }

        } catch (Exception ex) {
            LOG.error("释放连接", ex);
        }
    }


    //private Object transformType(DataType source){
    //    String toUpperCase = source.getLogicalType().getDefaultConversion().getSimpleName().toUpperCase();
    //    if(toUpperCase.equals("STRING")){
    //        partialRow.addString(fieldNames[i], rowData.getString(i).toString());
    //    }else if(toUpperCase.equals("LONG")){
    //        partialRow.addLong(fieldNames[i], rowData.getLong(i));
    //    }
    //}

}
