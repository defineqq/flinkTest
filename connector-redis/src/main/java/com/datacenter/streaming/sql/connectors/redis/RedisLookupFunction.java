package com.datacenter.streaming.sql.connectors.redis;

import com.datacenter.streaming.sql.connectors.redis.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.shaded.guava18.com.google.common.cache.Cache;
import org.apache.flink.shaded.guava18.com.google.common.cache.CacheBuilder;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.table.types.DataType;
import org.apache.flink.types.Row;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * Redis Lookup Function
 *
 * @author wangpei
 */
@Slf4j
public class RedisLookupFunction extends TableFunction<Row> {

    private static final long serialVersionUID = 1L;
    private final RedisUtils redisUtils = RedisUtils.INSTANCE;

    private String host;
    private int port;
    private int db = 0;
    private String password;
    private KeyType keyType;

    // Lookup字段
    // redis lookup dim只支持一个lookup键
    private final String lookupFieldName;
    // Lookup字段类型
    //private final DataType lookupFieldType;

    // 返回字段(包括Lookup字段)
    private final String[] returnFieldNames;
    // 返回字段类型
    private final DataType[] returnFieldTypes;
    private int lookupFieldIndex = -1;
    private List<Integer> columnIndex = new ArrayList<>();


    private Jedis redisClient;

    private final long cacheMaxSize;
    private final long cacheExpireMs;
    private final int maxRetryTimes;
    private transient Cache<Row, List<Row>> cache;

    public RedisLookupFunction(String host, int port, int db, String password, KeyType keyType,
                               String[] returnFieldNames, DataType[] returnFieldTypes, String lookupFieldName,
                               long cacheMaxSize, long cacheExpireMs, int maxRetryTimes) {
        this.host = host;
        this.port = port;
        this.db = db;
        this.password = password;
        this.keyType = keyType;

        this.returnFieldNames = returnFieldNames;
        this.returnFieldTypes = returnFieldTypes;
        this.lookupFieldName = lookupFieldName;

        this.cacheMaxSize = cacheMaxSize;
        this.cacheExpireMs = cacheExpireMs;
        this.maxRetryTimes = maxRetryTimes;

        for (int i = 0; i < returnFieldNames.length; i++) {
            String returnFieldName = returnFieldNames[i];
            if (lookupFieldName.equals(returnFieldName)) {
                lookupFieldIndex = i;
            } else {
                columnIndex.add(i);
            }
        }
    }

    @Override
    public void open(FunctionContext context) throws Exception {
        redisClient = redisUtils.obtainConn(redisClient, host, port, password, db);
        this.cache = ((cacheMaxSize == -1) || (cacheExpireMs == -1)) ? null : CacheBuilder.newBuilder()
                .expireAfterWrite(cacheExpireMs, TimeUnit.MILLISECONDS)
                .maximumSize(cacheMaxSize)
                .build();
    }

    @Override
    public void close() throws Exception {
        redisUtils.releaseConn(redisClient);
    }


    /**
     * 从Redis中获取具体Key对应的值
     *
     * @param keys
     */
    public void eval(Object... keys) {
        Object lookupKeyValue = keys[0];
        if (lookupKeyValue == null) return;

        Row keyRow = Row.of(lookupKeyValue);

        // 取缓存
        if (cache != null) {
            List<Row> cachedRows = cache.getIfPresent(keyRow);
            if (cachedRows != null) {
                for (Row cachedRow : cachedRows) {
                    collect(cachedRow);
                }
                return;
            }
        }

        Row row = new Row(returnFieldNames.length);
        row.setField(lookupFieldIndex, lookupKeyValue);

        for (int retry = 1; retry <= maxRetryTimes; retry++) {

            try {
                redisClient = redisUtils.obtainConn(redisClient, host, port, password, db);

                String lookupKeyValueString = keys[0].toString();

                // String类型
                if (keyType == KeyType.STRING) {
                    String value = redisClient.get(lookupKeyValueString);
                    fillFields(value, row);

                    // List类型
                } else if (keyType == KeyType.LIST) {
                    List<String> listValue = redisClient.lrange(lookupKeyValueString, 0, -1);
                    if (listValue.size() != 0) {
                        String value = String.join(",", listValue);
                        fillFields(value, row);

                    }
                    // Set类型
                } else if (keyType == KeyType.SET) {
                    Set<String> setValue = redisClient.smembers(lookupKeyValueString);
                    if (setValue.size() != 0) {
                        String value = String.join(",", setValue);
                        fillFields(value, row);
                    }
                    // Hash类型
                } else if (keyType == KeyType.HASH) {
                    Map<String, String> value = redisClient.hgetAll(lookupKeyValueString);
                    if (value.size() != 0) {
                        for (Integer index : columnIndex) {
                            String fieldName = returnFieldNames[index];
                            row.setField(index, value.getOrDefault(fieldName, null));
                        }
                    }
                }
                collect(row);
                if (cache != null) {
                    cache.put(keyRow, Collections.singletonList(row));
                }
                break;
            } catch (Exception e) {
                LOG.warn("Look up value from redis occur an exception , will retry. ", e);
                try {
                    Thread.sleep(1000 * retry);
                } catch (InterruptedException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }

    private void fillFields(String value, Row row) {
        for (Integer index : columnIndex) {
            row.setField(index, value);
        }
    }
}
