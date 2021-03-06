package com.datacenter.streaming.sql.connectors.redis;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.StatementSet;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.junit.Test;

/**
 * Sink Redis List
 * @author wangpei
 */
public class SinkRedisList {

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

        String sinkDDL = ""
                + "CREATE TABLE sink_redis ( "
                + "   userID varchar, "
                + "   record varchar, "
                + "  PRIMARY KEY (userID) NOT ENFORCED "
                + ") WITH ( "
                + "    'connector' = 'redis', "
                + "    'host' = 'localhost', "
                + "    'port' = '6379', "
                + "    'db' = '0', "
                + "    'keyType' = 'set' "
                + ")";

        //CONCAT_WS(',', userID,eventTime,eventType,productID,productPrice)
        String insertSQL = ""
                + "INSERT INTO sink_redis "
                + "SELECT userID, CONCAT_WS(',', userID,eventTime,eventType,productID,cast(productPrice as string)) as record FROM source_kafka";

        tableEnv.executeSql(sourceDDL);
        tableEnv.executeSql(sinkDDL);
        StatementSet statementSet = tableEnv.createStatementSet();
        statementSet.addInsertSql(insertSQL);
        statementSet.execute();
    }
}
