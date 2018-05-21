package ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Creates a PreparedStatement object for a given SQL code. This interface implements {@link AutoCloseable}
 * as to take care of closing the necessary resources after actions on the Statement has been performed.
 */
public interface PreparedStatementGenerator extends AutoCloseable {

    /**
     * Creates a PreparedStatement object for the SQL code this generator has been bound to.
     * This method always returns the same object.
     *
     * @return the generated prepared statement (one per instance)
     */
    PreparedStatement createStatement() throws SQLException;

    @Override
    void close() throws SQLException;

}
