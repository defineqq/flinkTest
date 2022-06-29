package com.datacenter.streaming.sql.connectors.kudu;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * KuduOptions
 * @author wangpei
 */
@Builder
@Getter
public class KuduOptions implements Serializable {
    private static final long serialVersionUID = 1L;
    private String master;
    private String table;
    private String username;
    private String password;
}
