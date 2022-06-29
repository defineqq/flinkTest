package com.datacenter.streaming.sql.connectors.redis;

/**
 * 支持的Redis Key类型
 *
 * @author wangpei
 */
public enum KeyType {
    STRING("STRING", "STRING类型"),
    HASH("HASH", "HASH类型"),
    LIST("LIST", "LIST类型"),
    SET("SET", "SET类型"),
    ;
    private String type;
    private String desc;

    KeyType(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

}
