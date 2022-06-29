//package com.cp.data.test;
//
//import com.cp.data.dataland.common.redis.RedisCommand;
//import com.cp.data.dataland.common.redis.RedisPool;
//import org.junit.Test;
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.JedisPool;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.atomic.AtomicInteger;
//
//public class JedisReflectTest {
//
//  @Test
//  public void testCommand() throws InterruptedException {
//    ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(20);
//    long begin = System.currentTimeMillis();
//    AtomicInteger integer = new AtomicInteger(0);
//    for (int i = 0; i < 100000; i++) {
//      threadPoolExecutor.submit(new Runnable() {
//        RedisCommand command = new RedisCommand("35.172.240.112", 5379);
//
//        @Override
//        public void run() {
//          if (integer.incrementAndGet() < 10000) {
//            command.build("set", "ab" + integer.get(), "val-" + integer).execute();
//            System.out.println(integer.get());
//          }
//        }
//      });
//    }
//    Thread.sleep(1000000);
//    System.out.println((System.currentTimeMillis() - begin) / 1000);
//
//  }
//
//  @Test
//  public void testJedisPool() {
//    JedisPool redisPool = new JedisPool("35.172.240.112", 5379);
//    try (Jedis jedis = redisPool.getResource()) {
//      long l = jedis.incr("xx");
//      System.out.println(l);
//    }
//  }
//
//  @Test
//  public void testRedisPool() {
//    RedisPool redisPool = new RedisPool("localhost:6379");
//    try (RedisCommand redis = redisPool.getResource()) {
//      System.out.println(redis.build("incr", "xxx", "1").get());
//      System.out.println(redis.build("scard", "xxx").get());
//    }
//  }
//
//
//}
