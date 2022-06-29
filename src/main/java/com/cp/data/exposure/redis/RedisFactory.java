//package com.cp.data.exposure.redis;
//
//import java.util.concurrent.atomic.AtomicReference;
//
//import org.apache.commons.pool2.PooledObject;
//import org.apache.commons.pool2.PooledObjectFactory;
//import org.apache.commons.pool2.impl.DefaultPooledObject;
//
//import redis.clients.jedis.HostAndPort;
//import redis.clients.jedis.exceptions.JedisException;
//
///**
// * PoolableObjectFactory custom impl.
// */
//class RedisFactory implements PooledObjectFactory<RedisCommand> {
//  private final AtomicReference<HostAndPort> hostAndPort = new AtomicReference<HostAndPort>();
//  private final int connectionTimeout;
//
//  public RedisFactory(final String host, final int port, final int connectionTimeout
//  ) {
//    this.hostAndPort.set(new HostAndPort(host, port));
//    this.connectionTimeout = connectionTimeout;
//  }
//
//  public void setHostAndPort(final HostAndPort hostAndPort) {
//    this.hostAndPort.set(hostAndPort);
//  }
//
//  @Override
//  public void activateObject(PooledObject<RedisCommand> pooledJedis) throws Exception {
//    //pooledJedis.getObject().build("select", "0").execute();
//  }
//
//  @Override
//  public void destroyObject(PooledObject<RedisCommand> pooledJedis) throws Exception {
//    final RedisCommand jedis = pooledJedis.getObject();
//    if (jedis.isConnected()) {
//      try {
//        try {
//          jedis.build("quit").execute();
//        } catch (Exception ex) {
//          ex.printStackTrace();
//        }
//        jedis.disconnect();
//      } catch (Exception ex) {
//        ex.printStackTrace();
//      }
//    }
//
//  }
//
//  @Override
//  public PooledObject<RedisCommand> makeObject() throws Exception {
//    final HostAndPort hostAndPort = this.hostAndPort.get();
//    final RedisCommand redisCommand = new RedisCommand(hostAndPort.getHost(), hostAndPort.getPort(), connectionTimeout);
//
//    try {
//      redisCommand.connect();
//    } catch (JedisException je) {
//      redisCommand.close();
//      throw je;
//    }
//
//    return new DefaultPooledObject<>(redisCommand);
//
//  }
//
//  @Override
//  public void passivateObject(PooledObject<RedisCommand> pooledJedis) throws Exception {
//  }
//
//  @Override
//  public boolean validateObject(PooledObject<RedisCommand> pooledJedis) {
//    final RedisCommand jedis = pooledJedis.getObject();
//    try {
//      HostAndPort hostAndPort = this.hostAndPort.get();
//
//      String connectionHost = jedis.getHost();
//      int connectionPort = jedis.getPort();
//
//      return hostAndPort.getHost().equals(connectionHost)
//          && hostAndPort.getPort() == connectionPort && jedis.isConnected()
//          && jedis.build("ping").getString().equals("PONG");
//    } catch (final Exception e) {
//      return false;
//    }
//  }
//}
