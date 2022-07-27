package com.github.defineqq.calcite.parser.tutorial.sample;


import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;

/**
 * @author congpeng
 * @SpringBootApplication 使用calcite解析sql解析实例
 */

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
