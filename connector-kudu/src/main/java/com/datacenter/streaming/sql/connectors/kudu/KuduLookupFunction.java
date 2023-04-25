package com.datacenter.streaming.sql.connectors.kudu;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.shaded.guava18.com.google.common.cache.Cache;
import org.apache.flink.shaded.guava18.com.google.common.cache.CacheBuilder;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.table.types.DataType;
import org.apache.flink.types.Row;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduPredicate;
import org.apache.kudu.client.KuduScanner;
import org.apache.kudu.client.KuduTable;
import org.apache.kudu.client.RowResult;
import org.apache.kudu.client.RowResultIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.apache.flink.util.Preconditions.checkArgument;


/**
 * Kudu Lookup Function
 *
 * @author wangpei
 */
@Slf4j
public class KuduLookupFunction extends TableFunction<Row> {
    public static final Logger LOGGER = LogManager.getLogger(KuduLookupFunction.class);


    private static final long serialVersionUID = 1L;

    private String master;
    private String table;
    // 返回字段(即表字段)
    private final String[] fieldNames;
    // 返回字段的类型(即表字段类型)
    private final DataType[] fieldTypes;
    // Lookup字段(需要在表字段中)
    private final String[] keyNames;
    // Lookup字段类型
    private final DataType[] keyTypes;


    private KuduClient kuduClient;
    private KuduTable kuduTable;

    private final long cacheMaxSize;
    private final long cacheExpireMs;
    private final int maxRetryTimes;
    private transient Cache<Row, List<Row>> cache;

    public KuduLookupFunction(
            String master,
            String table,
            String[] fieldNames,
            DataType[] fieldTypes,
            String[] keyNames,
            long cacheMaxSize,
            long cacheExpireMs,
            int maxRetryTimes) {
        this.master = master;
        this.table = table;
        this.fieldNames = fieldNames;
        this.fieldTypes = fieldTypes;
        this.keyNames = keyNames;
        List<String> nameList = Arrays.asList(fieldNames);
        this.keyTypes = Arrays.stream(keyNames)
                .map(s -> {
                    checkArgument(nameList.contains(s),
                            "keyName %s can't find in fieldNames %s.", s, nameList);
                    return fieldTypes[nameList.indexOf(s)];
                })
                .toArray(DataType[]::new);
        this.cacheMaxSize = cacheMaxSize;
        this.cacheExpireMs = cacheExpireMs;
        this.maxRetryTimes = maxRetryTimes;
    }

    @Override
    public void open(FunctionContext context) throws Exception {
        obtainConn();
        this.cache = ((cacheMaxSize == -1) || (cacheExpireMs == -1)) ? null : CacheBuilder.newBuilder()
                .expireAfterWrite(cacheExpireMs, TimeUnit.MILLISECONDS)
                .maximumSize(cacheMaxSize)
                .build();
    }

    @Override
    public void close() throws Exception {
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
            if (kuduClient != null) {
                kuduClient.close();
            }
        } catch (Exception ex) {
            LOGGER.error(ex);
        }

    }

    /**
     * 从Kudu获取具体Key对应的值
     *
     * @param keys
     */
    public void eval(Object... keys) {

        Row keyRow = Row.of(keys);
        if (cache != null) {
            List<Row> cachedRows = cache.getIfPresent(keyRow);
            if (cachedRows != null) {
                for (Row cachedRow : cachedRows) {
                    collect(cachedRow);
                }
                return;
            }
        }

        for (int retry = 1; retry <= maxRetryTimes; retry++) {
            // 检查并获取连接
            obtainConn();

            // 查询数据
            KuduScanner.KuduScannerBuilder kuduScannerBuilder = kuduClient.newScannerBuilder(kuduTable);
            try {
                for (int i = 0; i < keyNames.length; i++) {
                    String keyName = keyNames[i];
                    Object keyValue = keys[i];
                    ColumnSchema columnSchema = kuduTable.getSchema().getColumn(keyName);
                    KuduPredicate kuduPredicate = KuduPredicate.newInListPredicate(columnSchema, Collections.singletonList(keyValue));
                    kuduScannerBuilder.addPredicate(kuduPredicate);
                }
                KuduScanner kuduScanner = kuduScannerBuilder.build();

                // 扫描并遍历结果
                ArrayList<Row> rows = new ArrayList<>();
                while (kuduScanner.hasMoreRows()) {
                    RowResultIterator rowResults = kuduScanner.nextRows();
                    while (rowResults.hasNext()) {
                        Row row = new Row(fieldNames.length);
                        RowResult rowResult = rowResults.next();
                        for (int i = 0; i < fieldNames.length; i++) {
                            String fieldName = fieldNames[i];
                            String fieldType = fieldTypes[i].getLogicalType().getDefaultConversion().getSimpleName().toUpperCase();
                            boolean aNull = rowResult.isNull(fieldName);
                            if (!aNull) {
                                Object obtainValue = obtainValue(rowResult, fieldName, fieldType);
                                Object targetValue = transformType(obtainValue, fieldType);
                                row.setField(i, targetValue);
                            }
                        }
                        rows.add(row);
                        collect(row);
                    }
                }
                if (cache != null) {
                    cache.put(keyRow, rows);
                }
                break;
            } catch (Exception ex) {
                LOGGER.error("Get data from kudu exception. KuduTable: {} KuduField: {} KuduFieldValue: {}", table, Arrays.asList(keyNames).toString(), Arrays.asList(keys).toString(), ex);
                try {
                    Thread.sleep(1000 * retry);
                } catch (InterruptedException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }

    }


    /**
     * 获取Kudu中的字段值
     *
     * @param rowResult
     * @param sourceFieldName
     * @param sourceFieldType
     * @return
     */
    private Object obtainValue(RowResult rowResult, String sourceFieldName, String sourceFieldType) {

        Object targetValue = null;
        try {
            if (rowResult != null) {
                String sourceFieldTypeUpper = sourceFieldType.toUpperCase();
                switch (sourceFieldTypeUpper) {
                    case "STRING":
                        targetValue = rowResult.getString(sourceFieldName);
                        break;
                    case "INT":
                    case "INTEGER":
                        targetValue = rowResult.getInt(sourceFieldName);
                        break;
                    case "LONG":
                    case "BIGINT":
                        targetValue = rowResult.getLong(sourceFieldName);
                        break;
                    case "DOUBLE":
                        targetValue = rowResult.getDouble(sourceFieldName);
                        break;
                    case "FLOAT":
                        targetValue = rowResult.getFloat(sourceFieldName);
                        break;
                    case "BOOL":
                        targetValue = rowResult.getBoolean(sourceFieldName);
                        break;
                }
            }

        } catch (Exception ex) {
            throw new RuntimeException("获取Kudu字段值异常,sourceFieldName: " + sourceFieldName + " sourceFieldType: " + sourceFieldType + " rowResult: " + rowResult.rowToString());
        }
        return targetValue;
    }

    /**
     * 目标类型处理
     *
     * @param sourceValue
     * @param targetType
     * @return
     */
    private Object transformType(Object sourceValue, String targetType) {

        Object targetValue = null;

        try {
            if (sourceValue != null) {
                String sourceValueToString = sourceValue.toString();
                String targetTypeToUpperCase = targetType.toUpperCase();
                switch (targetTypeToUpperCase) {
                    case "STRING":
                        targetValue = sourceValueToString;
                        break;
                    case "INT":
                    case "INTEGER":
                    case "TINYINT":
                        targetValue = Integer.valueOf(sourceValueToString);
                        break;
                    case "DOUBLE":
                        targetValue = Double.valueOf(sourceValueToString);
                        break;
                    case "FLOAT":
                        targetValue = Float.valueOf(sourceValueToString);
                        break;
                    case "LONG":
                    case "BIGINT":
                        targetValue = Long.valueOf(sourceValueToString);
                        break;
                    case "BOOLEAN":
                        targetValue = Boolean.valueOf(sourceValueToString);
                        break;
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Kudu值向目标类型转换异常, targetType: " + targetType + " sourceValue: " + sourceValue.toString());
        }
        return targetValue;
    }

}
