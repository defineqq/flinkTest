//package com.cp.data.exposure.redis;
//
//import org.apache.commons.pool2.impl.GenericObjectPool;
//import redis.clients.jedis.Protocol;
//import redis.clients.util.JedisURIHelper;
//import redis.clients.util.Pool;
//
//import java.net.URI;
//
//public class RedisPool extends Pool<RedisCommand> {
//
//  public RedisPool(String host, int connectTimeout) {
//    host = "redis://" + host;
//    URI uri = URI.create(host);
//    if (JedisURIHelper.isValid(uri)) {
//      String ip = uri.getHost();
//      int port = uri.getPort();
//      this.internalPool = new GenericObjectPool<>(new RedisFactory(ip, port, connectTimeout));
//    } else  {
//      throw new RuntimeException(String.format("illegal host %s", host));
//    }
//  }
//
//  @Override
//  public RedisCommand getResource() {
//    RedisCommand command = super.getResource();
//    command.setDataSource(this);
//    return command;
//  }
//
//  public RedisPool(String host) {
//    this(host, Protocol.DEFAULT_TIMEOUT);
//  }
//
//
//}
