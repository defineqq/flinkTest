package com.joyme.test;

import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author congpeng
 * @SpringBootApplication 裱糊一个主程序类
 */
@SpringBootApplication
public class test {
    public static void main(String[] args) {
        SqlParser.Config config = SqlParser.configBuilder()
                .setLex(Lex.MYSQL) //使用mysql 语法
                .build();
//SqlParser 语法解析器
        SqlParser sqlParser = SqlParser
                .create("select id,name,age FROM stu where age<20", config);
        SqlNode sqlNode = null;
        try {
            sqlNode = sqlParser.parseStmt();
        } catch (SqlParseException e) {
            throw new RuntimeException("", e);
        }
    }
}
