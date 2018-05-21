package ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Contains the generated prepared statement. This interface wraps it such that {@link AutoCloseable} can be
 * implemented as to take care of closing the necessary resources after the action on the Statement has been performed.
 */
public interface PreparedStatementResult extends AutoCloseable {

    /**
     * @return the generated prepared statement
     */
    PreparedStatement getPreparedStatement();

    @Override
    void close() throws SQLException;

}
