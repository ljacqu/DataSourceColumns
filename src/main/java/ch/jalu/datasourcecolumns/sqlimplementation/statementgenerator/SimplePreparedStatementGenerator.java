package ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Simple implementation of {@link PreparedStatementGenerator} that closes the prepared statement afterwards.
 */
public class SimplePreparedStatementGenerator implements PreparedStatementGenerator {

    private final Connection connection;
    private final String sql;
    private PreparedStatement preparedStatement;

    public SimplePreparedStatementGenerator(Connection connection, String sql) {
        this.connection = connection;
        this.sql = sql;
    }

    @Override
    public PreparedStatement createStatement() throws SQLException {
        if (preparedStatement == null) {
            preparedStatement = connection.prepareStatement(sql);
        }
        return preparedStatement;
    }

    @Override
    public void close() throws SQLException {
        if (preparedStatement != null) {
            preparedStatement.close();
        }
    }
}
