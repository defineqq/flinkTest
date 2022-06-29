package com.datacenter.streaming.sql.connectors.redis.utils;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

/**
 * 单例工具类
 *
 * @author wangpei
 */
@Slf4j
public enum RedisUtils {

    INSTANCE;

    /**
     * 获取连接
     */
    public Jedis obtainConn(Jedis redisClient, String host, int port, String password, int db) {
        try {
            if (redisClient == null) {
                redisClient = new Jedis(host, port, 0);
                if (password != null) {
                    redisClient.auth(password);
                }
                redisClient.select(db);
            }
            return redisClient;
        } catch (Exception ex) {
            throw new RuntimeException("Get redis connection...", ex);
        }
    }

    /**
     * 释放连接
     */
    public void releaseConn(Jedis redisClient) {
        try {
            if (redisClient != null) {
                redisClient.close();
            }
        } catch (Exception ex) {
            LOG.error("Release redis connection...", ex);
        }

    }
}
