package com.datacenter.streaming.sql.connectors.redis;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.junit.Test;

/**
 * 从Redis Hash Key中获取维度数据
 * @author wangpei
 */
public class DimRedisHash {

    @Test
    public void test() throws Exception {
        StreamExecutionEnvironment streamEnv = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration());
        streamEnv.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(streamEnv);

        String sourceDDL = ""
                + "CREATE TABLE source_kafka ( "
                + "    userID VARCHAR, "
                + "    eventTime VARCHAR, "
                + "    eventType VARCHAR, "
                + "    productID VARCHAR, "
                + "    productPrice INT, "
                + "    proctime AS PROCTIME() "
                + ") WITH ( "
                + "    'connector' = 'kafka', "
                + "    'topic' = 'topic_1', "
                + "    'properties.bootstrap.servers' = 'defineqq_kafka_qa_01:9092', "
                + "    'properties.group.id' = 'flink111_v5', "
                + "    'format' = 'json', "
                + "    'scan.startup.mode' = 'latest-offset' "
                + ")";

        String dimDDL = ""
                + "CREATE TABLE dim_redis ( "
                + "   userID varchar, "
                + "   userAge varchar, "
                + "   userAddress varchar "
                + ") WITH ( "
                + "    'connector' = 'redis', "
                + "    'host' = 'localhost', "
                + "    'port' = '6379', "
                + "    'db' = '0', "
                + "    'keyType' = 'hash', "
                + "    'lookup.cache.ttl' = '30000000', "
                + "    'lookup.cache.max-rows' = '5' "
                + ")";

        String insertSQL = ""
                + "SELECT kafka.userID \n" +
                "       proctime,\n" +
                "       userAge,\n" +
                "       userAddress \n" +
                "FROM source_kafka AS kafka\n" +
                "         INNER JOIN dim_redis FOR SYSTEM_TIME AS OF kafka.proctime AS r\n" +
                "ON kafka.userID=r.userID";

        tableEnv.executeSql(sourceDDL);
        tableEnv.executeSql(dimDDL);

        tableEnv.toAppendStream(tableEnv.sqlQuery(insertSQL), Row.class).print();
        streamEnv.execute("aaa");
    }
}
