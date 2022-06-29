package com.datacenter.streaming.sql.connectors.redis;

import com.datacenter.streaming.sql.connectors.redis.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.io.RichOutputFormat;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.DataType;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Redis Output Format
 *
 * @author wangpei
 */
@Slf4j
public class RedisOutputFormat extends RichOutputFormat<RowData> {

    private static final long serialVersionUID = 1L;
    private final RedisUtils redisUtils = RedisUtils.INSTANCE;

    private String host;
    private int port;
    private int db = 0;
    private String password;
    private KeyType keyType;
    private int expireSeconds = -1;

    private final String[] fieldNames;
    private final DataType[] fieldTypes;
    // redis sink只支持一个主键
    private final String primaryKey;
    private int maxRetryTimes;

    Jedis redisClient;
    private int primaryKeyIndex = -1;
    private List<Integer> columnIndex = new ArrayList<>();

    public RedisOutputFormat(String host, int port, int db, String password, KeyType keyType, String[] fieldNames, DataType[] fieldTypes, String primaryKey, int maxRetryTimes, int expireSeconds) {
        this.host = host;
        this.port = port;
        this.db = db;
        this.password = password;
        this.keyType = keyType;
        this.fieldNames = fieldNames;
        this.fieldTypes = fieldTypes;
        this.primaryKey = primaryKey;
        this.maxRetryTimes = maxRetryTimes;
        this.expireSeconds = expireSeconds;
        for (int i = 0; i < fieldNames.length; i++) {
            String fieldName = fieldNames[i];
            if (fieldName.equals(primaryKey)) {
                primaryKeyIndex = i;
            } else {
                columnIndex.add(i);
            }
        }
    }

    @Override
    public void configure(Configuration configuration) {
    }

    @Override
    public void open(int taskNumber, int numTasks) {
        redisClient = redisUtils.obtainConn(redisClient, host, port, password, db);
    }

    @Override
    public void writeRecord(RowData rowData) {

        if (rowData.isNullAt(primaryKeyIndex)) return;
        String primaryKeyValue = getStringValue(fieldTypes[primaryKeyIndex], rowData, primaryKeyIndex);

        for (int retry = 1; retry <= maxRetryTimes; retry++) {
            try {
                redisClient = redisUtils.obtainConn(redisClient, host, port, password, db);
                if (keyType == KeyType.STRING) {
                    for (Integer index : columnIndex) {
                        if (!rowData.isNullAt(index)) {
                            String stringValue = getStringValue(fieldTypes[index], rowData, index);
                            if (expireSeconds != -1) {
                                redisClient.setex(primaryKeyValue, expireSeconds, stringValue);
                            } else {
                                redisClient.set(primaryKeyValue, stringValue);
                            }
                            break;
                        }
                    }
                } else if (keyType == KeyType.LIST) {
                    for (Integer index : columnIndex) {
                        if (!rowData.isNullAt(index)) {
                            String stringValue = getStringValue(fieldTypes[index], rowData, index);
                            redisClient.rpush(primaryKeyValue, stringValue);
                            if (expireSeconds != -1) {
                                redisClient.expire(primaryKeyValue, expireSeconds);
                            }
                            break;
                        }
                    }
                } else if (keyType == KeyType.SET) {
                    for (Integer index : columnIndex) {
                        if (!rowData.isNullAt(index)) {
                            String stringValue = getStringValue(fieldTypes[index], rowData, index);
                            redisClient.sadd(primaryKeyValue, stringValue);
                            if (expireSeconds != -1) {
                                redisClient.expire(primaryKeyValue, expireSeconds);
                            }
                            break;
                        }
                    }
                } else if (keyType == KeyType.HASH) {
                    HashMap<String, String> hashMap = new HashMap<>();
                    for (Integer index : columnIndex) {
                        if (!rowData.isNullAt(index)) {
                            String stringValue = getStringValue(fieldTypes[index], rowData, index);
                            hashMap.put(fieldNames[index], stringValue);
                        }
                    }
                    redisClient.hmset(primaryKeyValue, hashMap);
                    if (expireSeconds != -1) {
                        redisClient.expire(primaryKeyValue, expireSeconds);
                    }

                }
                break;
            } catch (Exception e) {
                LOG.warn("Write value to redis occur an exception , will retry. ", e);
                try {
                    Thread.sleep(1000 * retry);
                } catch (InterruptedException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        redisUtils.releaseConn(redisClient);
    }

    public String getStringValue(DataType fieldType, RowData rowData, int index) {
        String fieldTypeJava = fieldType.getConversionClass().getSimpleName().toUpperCase();
        if (fieldTypeJava.equals("STRING")) {
            return rowData.getString(index).toString();
        } else if (fieldTypeJava.equals("DOUBLE")) {
            double value = rowData.getDouble(index);
            return String.valueOf(value);
        } else if (fieldTypeJava.equals("BIGINT") || fieldTypeJava.equals("LONG")) {
            long value = rowData.getLong(index);
            return String.valueOf(value);
        } else {
            throw new RuntimeException("Unsupported data type: " + fieldTypeJava);
        }
    }
}
