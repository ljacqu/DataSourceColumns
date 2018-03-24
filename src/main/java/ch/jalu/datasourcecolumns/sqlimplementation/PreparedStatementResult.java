package ch.jalu.datasourcecolumns.sqlimplementation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PreparedStatementResult implements AutoCloseable {

    private final PreparedStatement preparedStatement;

    public PreparedStatementResult(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    /**
     * Convenience method for calling {@link PreparedStatement#setObject(int, Object)}
     * on the wrapped prepared statement.
     *
     * @param parameterIndex param index
     * @param x value to bind
     */
    public void setObject(int parameterIndex, Object x) throws SQLException {
        preparedStatement.setObject(parameterIndex, x);
    }

    /**
     * Convenience method for calling {@link PreparedStatement#executeQuery()}
     * on the wrapped prepared statement.
     *
     * @return result set
     */
    public ResultSet executeQuery() throws SQLException {
        return preparedStatement.executeQuery();
    }

    /**
     * Convenience method for calling {@link PreparedStatement#executeUpdate()}
     * on the wrapped prepared statement.
     *
     * @return number of affected rows
     */
    public int executeUpdate() throws SQLException {
        return preparedStatement.executeUpdate();
    }

    @Override
    public void close() throws SQLException {
        preparedStatement.close();
    }
}
