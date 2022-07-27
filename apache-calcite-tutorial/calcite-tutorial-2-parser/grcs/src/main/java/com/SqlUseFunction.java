package com;

import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.util.ImmutableNullableList;

import java.util.List;

public class SqlUseFunction extends SqlCall {

    private static final SqlSpecialOperator OPERATOR = new SqlSpecialOperator("USE FUNCTION",
            SqlKind.OTHER_FUNCTION);

    private final SqlIdentifier funcName;
    private final SqlNodeList funcProps;

    /**
     * SqlUseFunction constructor.
     *
     * @param pos sql define location
     * @param funcName function name
     * @param funcProps function property
     * */
    public SqlUseFunction(SqlParserPos pos, SqlIdentifier funcName, SqlNodeList funcProps) {
        super(pos);
        this.funcName = funcName;
        this.funcProps = funcProps;
    }

    @Override
    public void unparse(SqlWriter writer, int leftPrec, int rightPrec) {
        writer.keyword("USE FUNCTION");
        funcName.unparse(writer, leftPrec, rightPrec);
        if (funcProps != null) {
            writer.keyword("WITH");
            SqlWriter.Frame frame = writer.startList("(", ")");
            for (SqlNode c : funcProps) {
                writer.sep(",");
                c.unparse(writer, 0, 0);
            }
            writer.endList(frame);
        }
    }

    @Override
    public SqlOperator getOperator() {
        return OPERATOR;
    }

    @Override
    public List<SqlNode> getOperandList() {
        return ImmutableNullableList.of(funcName, funcProps);
    }

}