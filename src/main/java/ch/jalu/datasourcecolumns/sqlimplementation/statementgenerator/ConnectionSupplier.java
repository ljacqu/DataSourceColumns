package ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Supplier of connections to a database.
 */
@FunctionalInterface
public interface ConnectionSupplier {

    /**
     * Returns a connection to the database (typically from a connection pool).
     *
     * @return the connection
     */
    Connection get() throws SQLException;

}
