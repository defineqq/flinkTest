package com.datacenter.streaming.sql.connectors.redis;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.StatementSet;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.junit.Test;

/**
 * 从Redis Hash Key中获取维度数据
 *
 * @author wangpei
 */
public class SinkRedisHashV2 {

    @Test
    public void test() throws Exception {
        StreamExecutionEnvironment streamEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        EnvironmentSettings settings = EnvironmentSettings.newInstance().inStreamingMode().useBlinkPlanner().build();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(streamEnv, settings);

        String sourceDDL = ""
                + "CREATE TABLE source_kafka ( "
                + "    Data__resource_id STRING , "
                + "    Data__sender STRING , "
                + "    Data__type BIGINT , "
                + "    Data__ext STRING "
                + ") WITH ( "
                + "    'connector' = 'kafka', "
                + "    'topic' = 'cl_consume_receiver_log', "
                + "    'properties.bootstrap.servers' = '10.66.100.14:9092,10.66.102.69:9092,10.66.102.184:9092,10.66.104.241:9092,10.66.104.58:9092', "
                + "    'format' = 'json', "
                + "    'properties.group.id' = 'consumer-streaming_sql-1597583903', "
                + "    'scan.startup.mode' = 'latest-offset' "
                + ")";

        String sinkDDL = ""
                + "CREATE TABLE sink_redis ( "
                + "    vid STRING, "
                + "    total_sender_uv STRING, "
                + "    PRIMARY KEY (vid) NOT ENFORCED "
                + ") WITH ( "
                + "    'connector' = 'redis', "
                + "    'host' = 'kewldatacenter.ux4gcx.ng.0001.use1.cache.amazonaws.com', "
                + "    'port' = '6379', "
                + "    'db' = '15', "
                + "    'keyType' = 'hash' "
                + ")";

        String insertSQL = ""
                + "insert into sink_redis select "
                + "    vid, "
                + "    cast(count(distinct sender) as string) as total_sender_uv "
                + "from (select "
                + "        Data__resource_id as vid, "
                + "        Data__sender as sender, "
                + "        Data__type as type1, "
                + "        cast(UDFJsonExtractor(Data__ext,'is_official') as int) as is_official, "
                + "        cast(UDFJsonExtractor(Data__ext,'vid_type') as int) as vid_type "
                + "    from source_kafka "
                + " ) as t1 "
                + "where is_official=0 and vid_type=1 and type1 in (0,7) "
                + "group by vid";

        tableEnv.executeSql(sourceDDL);
        tableEnv.executeSql(sinkDDL);
        tableEnv.executeSql("CREATE FUNCTION UDFGetCurrentTime AS 'com.datacenter.streaming.sql.functions.udf.UDFGetCurrentTime'");
        tableEnv.executeSql("CREATE FUNCTION UDFJsonExtractor AS 'com.datacenter.streaming.sql.functions.udf.UDFJsonExtractor'");
        StatementSet statementSet = tableEnv.createStatementSet();
        statementSet.addInsertSql(insertSQL);
        statementSet.execute();

        //tableEnv.toRetractStream(tableEnv.sqlQuery(insertSQL), Row.class).print();
        //streamEnv.execute(this.getClass().getSimpleName());
    }
}
