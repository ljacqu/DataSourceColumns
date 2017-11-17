package ch.jalu.datasourcecolumns.sqlimplementation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementGenerator {

    /**
     * Creates a PreparedStatement with the given SQL code.
     *
     * @param sql the sql code to create a statement for
     * @return the generated PreparedStatement
     */
    PreparedStatement create(String sql) throws SQLException;

    /**
     * Creates a PreparedStatementGenerator based on the given connection.
     *
     * @param con the data source connection
     * @return created generator
     */
    static PreparedStatementGenerator fromConnection(Connection con) {
        return con::prepareStatement;
    }
}
