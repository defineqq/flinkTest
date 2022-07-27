package com.github.defineqq.calcite.parser.tutorial.sample.statement;

import com.github.defineqq.calcite.parser.tutorial.sample.AbstractSample;
import org.apache.calcite.sql.parser.SqlParseException;

/**
 * 文字解析
 * 解析字符串,数字,日期等文字信息
 *
 * @author defineqq
 * @date 2019-04-24 15:34:00
 */
public class LiteralSample extends AbstractSample {
    public static void main(String[] args) throws SqlParseException {
        System.out.println(parser.parseQuery("literal_sample 123").getClass());
        System.out.println(parser.parseQuery("literal_sample 'abc'").getClass());
        System.out.println(parser.parseQuery("literal_sample null").getClass());
        System.out.println(parser.parseQuery("literal_sample DATE '2004-10-22'").getClass());
        System.out.println(parser.parseQuery("literal_sample INTERVAL '1' SECOND").getClass());
    }
}
