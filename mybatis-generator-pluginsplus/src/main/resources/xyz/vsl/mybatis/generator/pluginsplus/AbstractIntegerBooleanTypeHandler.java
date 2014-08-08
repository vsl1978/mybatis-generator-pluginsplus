package ${PACKAGE};

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

abstract class AbstractIntegerBooleanTypeHandler implements TypeHandler {
    private int TRUE;
    private int FALSE;

    AbstractIntegerBooleanTypeHandler(int TRUE, int FALSE) {
        this.TRUE = TRUE;
        this.FALSE = FALSE;
    }

    private int toInteger(boolean value) {
        return value ? TRUE : FALSE;
    }

    private Boolean toBoolean(Integer value) {
        if (value == null) return null;
        if (TRUE == value.intValue()) return Boolean.TRUE;
        if (FALSE == value.intValue()) return Boolean.FALSE;
        return false;
    }

    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, Object o, JdbcType jdbcType) throws SQLException {
        Integer value = (o == null) ? null : toInteger(((Boolean)o).booleanValue());
        if (value == null)
            preparedStatement.setNull(i, jdbcType.TYPE_CODE);
        else
            preparedStatement.setInt(i, value.intValue());
    }

    @Override
    public Object getResult(ResultSet resultSet, String s) throws SQLException {
        int v = resultSet.getInt(s);
        return toBoolean(resultSet.wasNull() ? null : Integer.valueOf(v));
    }

    @Override
    public Object getResult(ResultSet resultSet, int i) throws SQLException {
        int v = resultSet.getInt(i);
        return toBoolean(resultSet.wasNull() ? null : Integer.valueOf(v));
    }

    @Override
    public Object getResult(CallableStatement callableStatement, int i) throws SQLException {
        int v = callableStatement.getInt(i);
        return toBoolean(callableStatement.wasNull() ? null : Integer.valueOf(v));
    }
}
