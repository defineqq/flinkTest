package com.github.defineqq.calcite.parser.tutorial.sample;

import defineqq.calcite.parser.tutorial.CustomSqlParserImpl;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.parser.SqlParser;

/**
 * @author defineqq
 * @date 2019-04-24 15:47:00
 */
public abstract class AbstractSample {
    public static SqlParser.Config mysqlConfig = SqlParser.configBuilder()
            // 定义解析工厂
            .setParserFactory(CustomSqlParserImpl.FACTORY)
            .setLex(Lex.MYSQL)
            .build();
    public static SqlParser parser = SqlParser.create("", mysqlConfig);

}
