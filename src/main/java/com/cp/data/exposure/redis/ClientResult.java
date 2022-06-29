//package com.cp.data.exposure.redis;
//
//import redis.clients.jedis.Protocol;
//
//import java.util.List;
//
//public class ClientResult {
//
//  private final RedisCommand conn;
//  private String command;
//  private String[] params;
//
//  public ClientResult(RedisCommand conn, String command, String... param) {
//    this.conn = conn;
//    this.command = command;
//    this.params = param;
//  }
//
//  public void execute() {
//    sendCommand();
//    conn.voidReply();
//  }
//
//  private void sendCommand() {
//    conn.sendCommand(Protocol.Command.valueOf(command), params);
//  }
//
//  public Long getLong() {
//    sendCommand();
//    return conn.getIntegerReply();
//  }
//
//  public String getString() {
//    sendCommand();
//    return conn.getBulkReply();
//  }
//
//  public Object get() {
//    sendCommand();
//    switch (Protocol.Command.valueOf(command)) {
//      case LLEN:
//      case GETBIT:
//      case BITOP:
//      case PUBLISH:
//      case OBJECT:
//      case BITCOUNT:
//      case SENTINEL:
//      case PEXPIRE:
//      case PTTL:
//      case PFADD:
//      case PFCOUNT:
//      case GEOADD:
//      case EXISTS:
//      case RENAME:
//      case RENAMEX:
//      case EXPIREAT:
//      case MOVE:
//      case SMOVE:
//      case MSETNX:
//      case DECRBY:
//      case DECR:
//      case INCR:
//      case INCRBY:
//      case APPEND:
//      case HINCRBY:
//      case HEXISTS:
//      case SCARD:
//      case SISMEMBER:
//      case SINTERSTORE:
//      case SUNIONSTORE:
//      case SDIFFSTORE:
//      case ZRANK:
//      case ZREVRANK:
//      case ZCARD:
//      case ZCOUNT:
//      case SETBIT:
//      case HSETNX:
//      case SETNX:
//      case SADD:
//      case ZADD:
//        return conn.getIntegerReply();
//      case BITFIELD:
//        return conn.getIntegerMultiBulkReply();
//      case PUBSUB:
//      case GEOHASH:
//      case MGET:
//      case HMGET:
//      case HVALS:
//      case LRANGE:
//      case SMEMBERS:
//      case SINTER:
//      case SUNION:
//      case ZRANGE:
//      case ZREVRANGE:
//      case SORT:
//      case BLPOP:
//      case BRPOP:
//      case ZRANGEBYSCORE:
//      case ZREVRANGEBYSCORE:
//      case ZRANGEBYLEX:
//        return conn.getMultiBulkReply();
//      case GET:
//      case RANDOMKEY:
//      case GETSET:
//      case INCRBYFLOAT:
//      case SUBSTR:
//      case HGET:
//      case LINDEX:
//      case LPOP:
//      case RPOP:
//      case RPOPLPUSH:
//      case SPOP:
//      case SRANDMEMBER:
//      case ZINCRBY:
//      case ZSCORE:
//      case ECHO:
//      case BRPOPLPUSH:
//      case GETRANGE:
//      case GEODIST:
//
//      case LREM:
//      case HSET:
//      case DEL:
//      case HLEN:
//      case SET:
//
//      case HDEL:
//      case RPUSH:
//      case LPUSH:
//      case SREM:
//      case ZREM:
//
//        return conn.getBulkReply();
//      default:
//        throw new RuntimeException(String.format("not suppport %s", command));
//    }
//  }
//
//  public List<String> getStrings() {
//    sendCommand();
//    return conn.getMultiBulkReply();
//  }
//
//  public List<Long> getLongs() {
//    sendCommand();
//    return conn.getIntegerMultiBulkReply();
//  }
//}
