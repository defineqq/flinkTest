package com.datacenter.streaming.sql.connectors.kudu;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Kudu Lookup options
 * @author wangpei
 */
@Builder
@Getter
public class KuduLookupOptions implements Serializable {
    private final long cacheMaxSize;
    private final long cacheExpireMs;
    private final int maxRetryTimes;
}
