//package com.cp.data.exposure.redis;
//
//import java.io.Serializable;
//import java.util.List;
//import java.util.stream.Collectors;
//
//
//import com.cp.data.exposure.UdfParser;
//import com.cp.data.exposure.bean.ImFollow;
//import com.cp.data.exposure.redis.RedisCommand;
//import com.cp.data.exposure.redis.RedisPool;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
//
///**
// * 配置 demo:
// * {
// * "exps": "sadd(xxx_${uid} ${timestamp})",
// * "server": "localhost:6379"
// * }
// */
//@Slf4j
//public class RedisSink extends RichSinkFunction<ImFollow> implements Serializable {
//
//  private List<String> exps;
//  private transient RedisPool redisPool;
//
//  @SuppressWarnings("unchecked")
//
//
//
//  public RedisSink() {
//
//  }
//
//  public RedisPool getRedisPool() {
//    if (redisPool == null) {
//      redisPool = new RedisPool("kewldatacenter.ux4gcx.ng.0001.use1.cache.amazonaws.com:6379");
//    }
//    return redisPool;
//  }
//
//  @SuppressWarnings("unchecked")
//
//  @Override
//  public void invoke(ImFollow im) {
//
//      String exp;
//      //user_relation_follow关注  user_relation_unfollow是取消关注
//
//        long currentTime = System.currentTimeMillis();  //获取当前时间
//        exp = "ZADD('data:cp:uid24exposure_"+im.getPosid()+im.getUid()+"','"+currentTime+"','"+im.getVid()+"')";
//
////      24小时曝光列表  像这种取数方式 ZRANGEBYSCORE data:cp:uid24exposure_80021316280497420247041 0 9999999999999
//
//      UdfParser.FuncNode funcNode = new UdfParser(exp).parse();
//      List<Object> params = funcNode.getParams()
//          .stream()
//          .map(param -> ((UdfParser.StaticNode) param).getValue())
//          .collect(Collectors.toList());
//      try (RedisCommand redisCommand = getRedisPool().getResource()) {
//        if (!params.isEmpty()) {
//          String[] stringParams = new String[params.size()];
//          for (int i = 0; i < params.size(); i++) {
//            stringParams[i] = params.get(i).toString();
//          }
//          redisCommand.build(funcNode.getFuncName(), stringParams).execute();
//        } else {
//          redisCommand.build(funcNode.getFuncName()).execute();
//        }
//      } catch (Exception e) {
//        log.error("异常：{}", e);
//      }
//
//  }
//
//  @Override
//  public void close() {
//      getRedisPool().close();
//  }
//}
