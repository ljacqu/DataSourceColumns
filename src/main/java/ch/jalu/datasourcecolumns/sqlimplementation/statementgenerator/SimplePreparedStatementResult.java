package ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Simple implementation of {@link PreparedStatementResult} that closes the prepared statement afterwards.
 */
public class SimplePreparedStatementResult implements PreparedStatementResult {

    private final PreparedStatement preparedStatement;

    public SimplePreparedStatementResult(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    @Override
    public void close() throws SQLException {
        preparedStatement.close();
    }
}
