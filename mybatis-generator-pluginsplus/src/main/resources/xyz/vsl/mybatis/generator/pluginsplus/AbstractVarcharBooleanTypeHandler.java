package ${PACKAGE};

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

abstract class AbstractVarcharBooleanTypeHandler implements TypeHandler {
    private String TRUE;
    private String FALSE;

    AbstractVarcharBooleanTypeHandler(String TRUE, String FALSE) {
        this.TRUE = TRUE;
        this.FALSE = FALSE;
    }

    private String toString(boolean value) {
        return value ? TRUE : FALSE;
    }

    private Boolean toBoolean(String value) {
        if (value == null) return null;
        if (TRUE.equals(value)) return Boolean.TRUE;
        if (FALSE.equals(value)) return Boolean.FALSE;
        if ("Y".equals(value) || "T".equals(value) || "1".equals(value)) return true;
        String low = value.toLowerCase();
        return "y".equals(low) || "t".equals(low) || "yes".equals(low) || "true".equals(low);
    }

    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, Object o, JdbcType jdbcType) throws SQLException {
        String value = (o == null) ? null : toString(((Boolean)o).booleanValue());
        if (value == null)
            preparedStatement.setNull(i, jdbcType.TYPE_CODE);
        else
            preparedStatement.setString(i, value);
    }

    @Override
    public Object getResult(ResultSet resultSet, String s) throws SQLException {
        return toBoolean(resultSet.getString(s));
    }

    @Override
    public Object getResult(ResultSet resultSet, int i) throws SQLException {
        return toBoolean(resultSet.getString(i));
    }

    @Override
    public Object getResult(CallableStatement callableStatement, int i) throws SQLException {
        return toBoolean(callableStatement.getString(i));
    }
}
