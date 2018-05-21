package ch.jalu.datasourcecolumns.demo;

import ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator.PreparedStatementGenerator;
import ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator.PreparedStatementGeneratorFactory;
import ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator.SingleConnectionPreparedStatementGenerator;

import java.sql.Connection;

public class LoggingStatementGeneratorFactory implements PreparedStatementGeneratorFactory {

    private final Connection connection;

    public LoggingStatementGeneratorFactory(Connection connection) {
        this.connection = connection;
    }

    @Override
    public PreparedStatementGenerator create(String sql) {
        System.out.println(" Generated SQL: " + sql);
        return new SingleConnectionPreparedStatementGenerator(connection, sql);
    }
}
