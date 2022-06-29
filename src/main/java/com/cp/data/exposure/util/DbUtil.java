package com.cp.data.exposure.util;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;


@Slf4j
public class DbUtil {

    public static Map<String, Object> rsItemToMap(ResultSet rs) throws SQLException {
        Map<String, Object> map = Maps.newHashMap();
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            String columnLabel = rs.getMetaData().getColumnLabel(i);
            map.put(columnLabel, rs.getObject(columnLabel));
        }
        return map;
    }

    public static List<Map<String, Object>> rsToListMap(ResultSet rs) throws SQLException {
        List<Map<String, Object>> totalList = Lists.newArrayList();
        while (rs.next()) {
            Map<String, Object> map = Maps.newHashMap();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                String columnLabel = rs.getMetaData().getColumnLabel(i);
                map.put(columnLabel, rs.getObject(columnLabel));
            }
            totalList.add(map);
        }
        return totalList;
    }

    public static void closeQuietly(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            log.warn("", e);
        }

        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            log.warn("", e);
        }
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            log.warn("", e);
        }
    }
}
