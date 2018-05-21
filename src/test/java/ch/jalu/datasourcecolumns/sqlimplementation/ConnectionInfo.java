package ch.jalu.datasourcecolumns.sqlimplementation;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Contains connection information to a database, which is either a single connection
 * or a connection pool.
 */
public class ConnectionInfo {

    private final Connection connection;
    private final HikariDataSource hikariDataSource;

    public ConnectionInfo(Connection connection) {
        this.connection = connection;
        this.hikariDataSource = null;
    }

    public ConnectionInfo(HikariDataSource hikariDataSource) {
        this.connection = null;
        this.hikariDataSource = hikariDataSource;
    }

    /**
     * Creates the appropriate SQL columns handler configuration based on the connection type.
     *
     * @param tableName the table name
     * @param idColumn the id column
     * @param context the context
     * @param <C> the context type
     * @return generated config
     */
    public <C> SqlColumnsHandlerConfig<C> createHandlerConfig(String tableName, String idColumn, C context) {
        if (connection != null) {
            return SqlColumnsHandlerConfig.forSingleConnection(connection,
                tableName, idColumn, context);
        } else if (hikariDataSource != null) {
            return SqlColumnsHandlerConfig.forConnectionPool(hikariDataSource::getConnection,
                tableName, idColumn, context);
        } else {
            throw new IllegalStateException("Connection and connection supplier may not both be null");
        }
    }

    /**
     * Executes the given SQL statements on the table.
     *
     * @param statements the statements to execute
     */
    public void executeStatements(String... statements) throws SQLException {
        if (hikariDataSource != null) {
            Connection connection = hikariDataSource.getConnection();
            executeStatementsInternal(connection, statements);
            connection.close();
        } else {
            executeStatementsInternal(this.connection, statements);
        }
    }

    /**
     * Closes the connection to the database.
     */
    public void closeConnection() throws SQLException {
        if (connection != null) {
            connection.close();
        }
        if (hikariDataSource != null) {
            hikariDataSource.close();
        }
    }

    private void executeStatementsInternal(Connection connection, String... statements) throws SQLException {
        try (Statement st = connection.createStatement()) {
            for (String statement : statements) {
                st.execute(statement);
            }
        }
    }
}
