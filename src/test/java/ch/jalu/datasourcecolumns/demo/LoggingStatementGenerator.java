package ch.jalu.datasourcecolumns.demo;

import ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator.PreparedStatementGenerator;
import ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator.PreparedStatementResult;
import ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator.SimplePreparedStatementResult;

import java.sql.Connection;
import java.sql.SQLException;

public class LoggingStatementGenerator implements PreparedStatementGenerator {

    private final Connection connection;

    public LoggingStatementGenerator(Connection connection) {
        this.connection = connection;
    }

    @Override
    public PreparedStatementResult create(String sql) throws SQLException {
        System.out.println(" Generated SQL: " + sql);
        return new SimplePreparedStatementResult(connection.prepareStatement(sql));
    }
}
