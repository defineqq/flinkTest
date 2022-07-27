package com.github.defineqq.calcite.parser.tutorial.sample.statement;

import com.github.defineqq.calcite.parser.tutorial.sample.AbstractSample;
import org.apache.calcite.sql.parser.SqlParseException;

/**
 * @author defineqq
 * @date 2019-04-25 11:19:00
 */
public class IntervalQualifierSample extends AbstractSample {
    public static void main(String[] args) throws SqlParseException {
        System.out.println(parser.parseQuery("interval_qualifier_sample MONTH").getClass());
    }
}
