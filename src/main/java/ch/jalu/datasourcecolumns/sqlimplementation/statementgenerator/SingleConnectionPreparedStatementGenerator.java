package ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Simple implementation of {@link PreparedStatementGenerator} when a single, long-living Connection object
 * should be used to create all prepared statements.
 */
public class SingleConnectionPreparedStatementGenerator implements PreparedStatementGenerator {

    private final Connection connection;
    private final String sql;
    private PreparedStatement preparedStatement;

    public SingleConnectionPreparedStatementGenerator(Connection connection, String sql) {
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
