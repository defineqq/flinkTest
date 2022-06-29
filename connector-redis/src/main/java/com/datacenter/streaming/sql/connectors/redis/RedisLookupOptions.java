package com.datacenter.streaming.sql.connectors.redis;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Redis Loopup options
 *
 * @author wangpei
 */
@Builder
@Data
public class RedisLookupOptions implements Serializable {
    private final long cacheMaxSize;
    private final long cacheExpireMs;
    private final int maxRetryTimes;
}
