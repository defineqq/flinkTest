package com.datacenter.streaming.sql.connectors.kudu;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Kudu sink options
 * @author wangpei
 */
@Builder
public class KuduSinkOptions implements Serializable {
    @Getter
    private final long batchIntervalMs;
    @Getter
    private final int maxRetries;
}
