package ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Generates prepared statement objects using a connection retrieved from a connection pool and ensures that
 * both the retrieved connection and the generated statement are closed.
 */
public class ConnectionPoolPreparedStatementGenerator implements PreparedStatementGenerator {

    private final ConnectionSupplier connectionSupplier;
    private final String sql;
    private Connection connection;
    private PreparedStatement preparedStatement;

    public ConnectionPoolPreparedStatementGenerator(ConnectionSupplier connectionSupplier, String sql) {
        this.connectionSupplier = connectionSupplier;
        this.sql = sql;
    }

    @Override
    public PreparedStatement createStatement() throws SQLException {
        if (connection == null) {
            connection = connectionSupplier.get();
        }
        if (preparedStatement == null) {
            preparedStatement = connection.prepareStatement(sql);
        }
        return preparedStatement;
    }

    @Override
    public void close() throws SQLException {
        try {
            if (connection != null) {
                connection.close();
            }
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        }
    }
}
