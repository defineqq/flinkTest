package com.datacenter.streaming.sql.connectors.redis;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Redis Common Options
 *
 * @author wangpei
 */
@Data
@Builder
public class RedisOptions implements Serializable {
    private static final long serialVersionUID = 1L;
    private String host;
    private int port;
    private int db;
    private String password;
    private KeyType keyType;
}
