package ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator;

import java.sql.Connection;

/**
 * Creates {@link PreparedStatementGenerator} objects.
 */
@FunctionalInterface
public interface PreparedStatementGeneratorFactory {

    /**
     * Creates a PreparedStatement generator for the given SQL code.
     *
     * @param sql the sql code to create a generator for
     * @return the generator for the given sql code
     */
    PreparedStatementGenerator create(String sql);

    /**
     * Creates a generator factory based on the given connection.
     *
     * @param con the data source connection
     * @return created generator
     */
    static PreparedStatementGeneratorFactory fromConnection(Connection con) {
        return sql -> new SingleConnectionPreparedStatementGenerator(con, sql);
    }

    /**
     * Creates a generator factory which uses the given connection supplier (typically a connection pool).
     *
     * @param connectionSupplier the supplier to use
     * @return created generator
     */
    static PreparedStatementGeneratorFactory fromConnectionPool(ConnectionSupplier connectionSupplier) {
        return sql -> new ConnectionPoolPreparedStatementGenerator(connectionSupplier, sql);
    }
}
