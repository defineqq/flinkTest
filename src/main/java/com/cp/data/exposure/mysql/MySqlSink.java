package com.cp.data.exposure.mysql;


import com.cp.data.exposure.util.DbUtil;
import com.cp.data.exposure.util.TemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;


import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * 配置demo:
 * {
 * "conn": {
 * "jdbc": "jdbc:mysql:/xxxx...",
 * "user": "xx"
 * "pwd": "pwdxx"
 * },
 * "sqls": ["insert t_xx values('${id}', '${name}')"]
 * }
 */
@Slf4j
public class MySqlSink extends RichSinkFunction<Tuple2<String,Long>> implements Serializable {

    private List<String> sqls;
    private Map<String, Object> connMap;
    private Connection connection = null;


    public MySqlSink(Map<String, Object> properties) {

        connMap = (Map<String, Object>) properties.get("conn");



    }

    public Connection getConn() {
        try {

            if (Objects.isNull(connection) || connection.isClosed()) {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection(
                        connMap.get("jdbc").toString(),
                        connMap.get("user").toString(),
                        connMap.get("pwd").toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Tuple2<String,Long> im) {
        List<String> sqls = new ArrayList<String>();

        
        String vid = im.f0;
        long count = im.f1;
        //没用上 但是没改 结构保留 可以扩展
        sqls.add("REPLACE INTO cp_ads_video_user_most(vid,online_cnt,minute_time) VALUES(${vid},${uid},${minute_time}) ");

        Map<String, Object> msgMap = new HashMap<>();

        msgMap.put("vid",vid);
        msgMap.put("count",count);

        PreparedStatement stmt = null;
        try {
            Connection conn = getConn();
            conn.setAutoCommit(false);
            for (String sql : sqls) {
//                "REPLACE INTO cl_user_finan_logs(id,uid,action,delta,created_at,updated_at,country) VALUES(${id},${uid},${action},${delta},${created_at},${updated_at},${country}) "
                List<String> variables = TemplateUtil.getVariableNames(sql);

                long minuteTime = System.currentTimeMillis();
                String sqll = "REPLACE INTO cp_ads_video_user_most(vid,online_cnt,minute_time) VALUES("+vid+","+count+","+minuteTime+") ";
                stmt = conn.prepareStatement(sqll);
//                for (int i = 1; i <= variables.size(); i++) {
//                    stmt.setObject(i, msgMap.get(variables.get(i - 1)));
//                }
                long start = System.nanoTime();
                stmt.execute();
                log.info("sql: {}, param: {}, msg:{}, cost: {} ms",
                        sqll, variables, msgMap, (System.nanoTime() - start) / 1000000);
            }
            conn.commit();
        } catch (SQLException e) {

            log.error(e.toString());
        } finally {
            DbUtil.closeQuietly(null, stmt, null);
        }
    }

    @Override
    public void close() {
        DbUtil.closeQuietly(connection, null, null);
    }
}
