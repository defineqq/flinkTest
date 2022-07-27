package com.github.defineqq.calcite.parser.tutorial.sample.statement;

import com.github.defineqq.calcite.parser.tutorial.sample.AbstractSample;
import org.apache.calcite.sql.parser.SqlParseException;

/**
 * @author defineqq
 * @date 2019-04-25 11:31:00
 */
public class SimpleIdentifierSample extends AbstractSample {
    public static void main(String[] args) throws SqlParseException {
        System.out.println(parser.parseQuery("simple_identifier_sample aa").getClass());
    }
}
