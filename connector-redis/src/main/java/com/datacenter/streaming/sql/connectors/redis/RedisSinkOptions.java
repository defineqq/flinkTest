package com.datacenter.streaming.sql.connectors.redis;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Redis Sink options
 *
 * @author wangpei
 */
@Data
@Builder
public class RedisSinkOptions implements Serializable {
    private final long batchIntervalMs;
    private final int maxRetries;
    private final int expireSeconds;
}
