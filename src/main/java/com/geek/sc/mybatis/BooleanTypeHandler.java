package com.geek.sc.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BooleanTypeHandler extends BaseTypeHandler<Boolean> {

    private static final int TRUE_NUMBER = 1;
    private static final int FALSE_NUMBER = 0;

    private Boolean valueOf(int value) throws SQLException {

        if (TRUE_NUMBER == value) {
            return new Boolean(true);
        } else if (FALSE_NUMBER == value) {
            return new Boolean(false);
        } else {
            throw new SQLException("Unexpected value "
                    + value
                    + " found where "
                    + TRUE_NUMBER
                    + " or "
                    + FALSE_NUMBER
                    + " was expected.");
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Boolean aBoolean, JdbcType jdbcType) throws SQLException {
        if (aBoolean != null) {
            preparedStatement.setInt(i, aBoolean ? TRUE_NUMBER : FALSE_NUMBER);
        }
    }

    @Override
    public Boolean getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return valueOf(resultSet.getInt(s));
    }

    @Override
    public Boolean getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return valueOf(resultSet.getInt(i));
    }

    @Override
    public Boolean getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return valueOf(callableStatement.getInt(i));
    }
}