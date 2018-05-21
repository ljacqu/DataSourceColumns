package ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Creates {@link PreparedStatementResult} objects.
 */
@FunctionalInterface
public interface PreparedStatementGeneratorFactory {

    /**
     * Creates a PreparedStatement generator for the given SQL code.
     *
     * @param sql the sql code to create a generator for
     * @return the generator for the given sql code
     */
    PreparedStatementResult create(String sql) throws SQLException;

    /**
     * Creates a generator factory based on the given connection.
     *
     * @param con the data source connection
     * @return created generator
     */
    static PreparedStatementGeneratorFactory fromConnection(Connection con) {
        return sql -> new SimplePreparedStatementResult(con.prepareStatement(sql));
    }
}
