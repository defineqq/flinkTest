//package com.cp.data.exposure.redis;
//
//import redis.clients.jedis.Connection;
//import redis.clients.jedis.Protocol;
//import redis.clients.util.Pool;
//
//public class RedisCommand extends Connection {
//
//  protected Pool<RedisCommand> dataSource = null;
//
//  public RedisCommand(String host, int port) {
//    super.setHost(host);
//    super.setPort(port);
//  }
//
//  public RedisCommand(String host, int port, int timeout) {
//    super.setHost(host);
//    super.setPort(port);
//    super.setConnectionTimeout(timeout);
//  }
//
//  public ClientResult build(String command, String... param) {
//    return new ClientResult(this, command.trim().toUpperCase(), param);
//  }
//
//  @Override
//  protected Connection sendCommand(Protocol.Command cmd, String... args) {
//    return super.sendCommand(cmd, args);
//  }
//
//  @Override
//  protected Connection sendCommand(Protocol.Command cmd) {
//    return super.sendCommand(cmd);
//  }
//
//  public void voidReply() {
//    super.resetPipelinedCount();
//    flush();
//  }
//
//  public void setDataSource(Pool<RedisCommand> dataSource) {
//    this.dataSource = dataSource;
//  }
//
//  @Override
//  public void close() {
//    if (dataSource != null) {
//      if (this.isBroken()) {
//        this.dataSource.returnBrokenResource(this);
//      } else {
//        this.dataSource.returnResource(this);
//      }
//    }
//  }
//}
