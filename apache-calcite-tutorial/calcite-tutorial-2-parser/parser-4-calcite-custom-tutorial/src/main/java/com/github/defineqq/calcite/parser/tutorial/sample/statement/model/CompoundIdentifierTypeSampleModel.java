package com.github.defineqq.calcite.parser.tutorial.sample.statement.model;

import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.parser.SqlParserPos;

import java.util.List;

/**
 * @author defineqq
 * @date 2019-04-25 11:09:00
 */
public class CompoundIdentifierTypeSampleModel extends SqlCall {

    private List<List<SqlNode>> all;

    public CompoundIdentifierTypeSampleModel(List<List<SqlNode>> all,SqlParserPos pos) {
        super(pos);
        this.all = all;
    }

    @Override
    public SqlOperator getOperator() {
        return null;
    }

    @Override
    public List<SqlNode> getOperandList() {
        return null;
    }

    public List<List<SqlNode>> getAll() {
        return all;
    }
}
