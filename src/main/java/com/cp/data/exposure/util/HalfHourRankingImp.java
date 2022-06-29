package com.cp.data.exposure.util;


import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import redis.clients.jedis.Jedis;
import java.util.Map;

/**
 * Author: Wang Pei
 * Summary:
 * <p>
 * todo
 *  1、需要改成连接池，注意连接池存在的问题
 *  2、最好用Redis事务 或 自定义实现 CheckpointedFunction+State
 */
@Slf4j
@Builder
public class HalfHourRankingImp extends RichSinkFunction<JSONObject> implements CheckpointedFunction {

    private String dataRedisHost;
    private Integer dataRedisPort;
    private Integer dataRedisDB;
    private Jedis dataJedis;

    private String flagRedisHost;
    private Integer flagRedisPort;
    private Integer flagRedisDB;
    private Jedis flagJedis;

    private static final String FLAGKEYTag="flag:CountryPageHalfHourRanking:{date}:{traceId}";
    private static final int FLAGKEYTTL=30 * 60;
    private static final String FLAGSUCCESS="success";

    // 1、每个receiver收了多少礼---收礼---全球榜单
    private static final String CMD1KEY="ranking:diamond:world:{date}";
    // 2、每个receiver收了多少礼---收礼---国家榜单
    private static final String CMD2KEY="ranking:diamond:{country}:{date}";
    // 3、每个receiver收到每个sender送了多少礼---收礼---个人榜
    private static final String CMD3KEY="ranking:diamond:gift:{date}:{receiver}";
    // 4、每个sender送了多少礼---送礼---全球榜单
    private static final String CMD4KEY="ranking:gift:world:{date}";
    // 5、每个sender送了多少礼---送礼---国家榜单
    private static final String CMD5KEY="ranking:gift:{country}:{date}";

    private String flagKey;
    private Map<String, String> acks;
    private String event;
    private Exception exception;

    @Override
    public void open(Configuration parameters) throws Exception {
        try {
            dataJedis = connWithRedis(dataRedisHost, dataRedisPort, dataRedisDB, 0);
            flagJedis = connWithRedis(flagRedisHost, flagRedisPort, flagRedisDB, 0);
        }catch (Exception ex){
            exception=ex;
            log.error("Redis连接没有正确初始化: ", ex);
            throw ex;
        }

    }

    @Override
    public void close() throws Exception {
        try {
            closeRedis(dataJedis);
            closeRedis(flagJedis);
        }catch (Exception ex){
            exception=ex;
            log.error("Redis连接没有正确关闭: ",ex);
            throw ex;
        }

    }

    private Jedis connWithRedis(String host, Integer port, Integer db, Integer timeout) {
        try {
            log.info("获取Redis连接 ... host: "+host+" port: "+port+" db: "+db);
            Jedis jedis = new Jedis(host, port, timeout);
            jedis.select(db);
            return jedis;
        }catch (Exception ex){
            log.error("获取Redis连接异常: ", ex);
            throw ex;
        }
    }

    private void closeRedis(Jedis jedis){
        try {
            log.info("关闭Redis连接 ...");
            if(jedis!=null){
                jedis.close();
            }
        }catch (Exception ex){
            log.error("关闭Redis连接异常: ",ex);
            throw ex;
        }

    }

    private void checkErrors() throws Exception {
        Exception e = exception;
        if (e != null) {
            exception = null;
            throw new Exception("处理异常.... 终止任务.... " + e.getMessage(), e);
        }
    }

    private Map<String,String> getAcks(String key){
        if (!flagJedis.isConnected()) {
            flagJedis = connWithRedis(flagRedisHost,flagRedisPort,flagRedisDB,0);
        }
        return flagJedis.hgetAll(key);
    }

    private void setAcks(String cmdKey){
        if (!flagJedis.isConnected()) {
            flagJedis = connWithRedis(flagRedisHost,flagRedisPort,flagRedisDB,0);
        }
        flagJedis.hset(flagKey,cmdKey,FLAGSUCCESS);
        flagJedis.expire(flagKey,FLAGKEYTTL);
    }



    /**
     * @param value
     * @param context
     * @throws Exception
     */
    @Override
    public void invoke(JSONObject value, Context context)  {

        try {
            // 获取数据
            String sender = value.getString("sender");
            String senderCountry = value.getString("senderCountry");
            String receiver = value.getString("receiver");
            String receiverCountry = value.getString("receiverCountry");
            String date = value.getString("date");
            Double delta = value.getDouble("price");
            String traceId = value.getString("traceId");
            event=value.getJSONObject("event").toJSONString();

            // 获取Ack
            flagKey = FLAGKEYTag.replace("{date}",date).replace("{traceId}", traceId);
            acks = getAcks(flagKey);

            receiveGiftRanking1(receiver, date, delta);
            receiveGiftRanking2(receiver,receiverCountry,date,delta);
            receiveGiftRanking3(sender,receiver,date,delta);
            sendGiftRanking1(sender,date,delta);
            sendGiftRanking2(sender,senderCountry,date,delta);
        } catch (Exception ex){
            if(ex instanceof NullPointerException){
                log.error("处理数据异常: {}",value.toJSONString(), ex);
            } else {
                log.error("处理数据异常: ",ex);
                exception=ex;
                throw ex;
            }
        }
    }

    @Override
    public void snapshotState(FunctionSnapshotContext context) throws Exception {
//        // the keyed state is always up to date anyways
//        // just bring the per-partition state in shape
//        countPerPartition.clear();
//        countPerPartition.add(localCount);
//————————————————
//        版权声明：本文为CSDN博主「二十六画生的博客」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
//        原文链接：https://blog.csdn.net/u010002184/article/details/106960911
        checkErrors();
    }

    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {
        // nothing to do.
//        // get the state data structure for the per-key state
//        countPerKey = context.getKeyedStateStore().getReducingState(
//                new ReducingStateDescriptor<>("perKeyCount", new AddFunction<>(), Long.class));
//
//        // get the state data structure for the per-partition state
//        countPerPartition = context.getOperatorStateStore().getOperatorState(
//                new ListStateDescriptor<>("perPartitionCount", Long.class));
//
//        // initialize the "local count variable" based on the operator state
//        for (Long l : countPerPartition.get()) {
//            localCount += l;
//        }
//
//————————————————
//        版权声明：本文为CSDN博主「二十六画生的博客」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
//        原文链接：https://blog.csdn.net/u010002184/article/details/106960911
    }

    // 每个人收了多少礼---收礼---全球榜单
    private void receiveGiftRanking1(String receiver, String date, Double delta) {

        if(acks==null || (!acks.getOrDefault(CMD1KEY,"").equals(FLAGSUCCESS))){

            if (!dataJedis.isConnected()) {
                dataJedis = connWithRedis(dataRedisHost,dataRedisPort,dataRedisDB,0);
            }

            String key = CMD1KEY.replace("{date}",date);
            Double oldScore = dataJedis.zscore(key, receiver);
            double newScore;

            if (oldScore == null) {
                newScore = 0 + delta;
            } else {
                newScore = oldScore + delta;
            }

            dataJedis.zadd(key, newScore, receiver);
            dataJedis.expire(key, 86400);

            setAcks(CMD1KEY);
        }else {
            log.warn("该条数据已经写入Redis过... RedisKey: "+CMD1KEY +" TraceID: "+flagKey +" Event: "+event);
        }
    }

    // 每个人收了多少礼---收礼---国家榜单
    private void receiveGiftRanking2(String receiver,String receiverCountry,String date,Double delta){

        if(acks==null || (!acks.getOrDefault(CMD2KEY,"").equals(FLAGSUCCESS))){

            if (!dataJedis.isConnected()) {
                dataJedis = connWithRedis(dataRedisHost,dataRedisPort,dataRedisDB,0);
            }


            String key=CMD2KEY.replace("{country}",receiverCountry).replace("{date}",date);
            Double oldScore = dataJedis.zscore(key, receiver);
            double newScore;

            if(oldScore==null){
                newScore =  0 + delta;
            }else {
                newScore = oldScore + delta;
            }
            dataJedis.zadd(key,newScore,receiver);
            dataJedis.expire(key,86400);

            setAcks(CMD2KEY);
        }else {
            log.warn("该条数据已经写入Redis过... RedisKey: "+CMD2KEY +" TraceID: "+flagKey +" Event: "+event);
        }
    }

    // 每个receiver收到每个sender送的多少礼---收礼---个人榜
    private void receiveGiftRanking3(String sender, String receiver, String date, Double delta) {

        if(acks==null || (!acks.getOrDefault(CMD3KEY,"").equals(FLAGSUCCESS))) {
            if (!dataJedis.isConnected()) {
                dataJedis = connWithRedis(dataRedisHost, dataRedisPort, dataRedisDB, 0);
            }

            String key=CMD3KEY.replace("{date}",date).replace("{receiver}",receiver);
            Double oldScore = dataJedis.zscore(key, sender);
            double newScore;

            if (oldScore == null) {
                newScore = 0 + delta;
            } else {
                newScore = oldScore + delta;
            }
            dataJedis.zadd(key, newScore, sender);
            dataJedis.expire(key, 86400);

            setAcks(CMD3KEY);
        }else {
            log.warn("该条数据已经写入Redis过... RedisKey: "+CMD3KEY +" TraceID: "+flagKey +" Event: "+event);
        }
    }


    // 每个人送了多少礼---送礼---全球榜单
    private void sendGiftRanking1(String sender, String date, Double delta) {

        if (acks == null || (!acks.getOrDefault(CMD4KEY,"").equals(FLAGSUCCESS))) {
            if (!dataJedis.isConnected()) {
                dataJedis = connWithRedis(dataRedisHost, dataRedisPort, dataRedisDB, 0);
            }

            String key=CMD4KEY.replace("{date}",date);
            Double oldScore = dataJedis.zscore(key, sender);
            double newScore;

            if (oldScore == null) {
                newScore = 0 + delta;
            } else {
                newScore = oldScore + delta;
            }
            dataJedis.zadd(key, newScore, sender);
            dataJedis.expire(key, 86400);

            setAcks(CMD4KEY);
        }else {
            log.warn("该条数据已经写入Redis过... RedisKey: "+CMD4KEY +" TraceID: "+flagKey +" Event: "+event);
        }
    }


    // 每个人送了多少礼---送礼---国家榜单
    public void sendGiftRanking2(String sender, String senderCountry, String date, Double delta) {

        if (acks == null || (!acks.getOrDefault(CMD5KEY,"").equals(FLAGSUCCESS))) {
            if (!dataJedis.isConnected()) {
                dataJedis = connWithRedis(dataRedisHost, dataRedisPort, dataRedisDB, 0);
            }

            String key = CMD5KEY.replace("{country}", senderCountry).replace("{date}", date);
            Double oldScore = dataJedis.zscore(key, sender);
            double newScore;
            if (oldScore == null) {
                newScore = 0 + delta;
            } else {
                newScore = oldScore + delta;
            }
            dataJedis.zadd(key, newScore, sender);
            dataJedis.expire(key, 86400);

            setAcks(CMD5KEY);
        }else {
            log.warn("该条数据已经写入Redis过... RedisKey: "+CMD5KEY +" TraceID: "+flagKey +" Event: "+event);
        }
    }

}
