package ch.jalu.datasourcecolumns.demo;

import ch.jalu.datasourcecolumns.sqlimplementation.PreparedStatementGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LoggingStatementGenerator implements PreparedStatementGenerator {

    private final Connection connection;

    public LoggingStatementGenerator(Connection connection) {
        this.connection = connection;
    }

    @Override
    public PreparedStatement create(String sql) throws SQLException {
        System.out.println(" Generated SQL: " + sql);
        return connection.prepareStatement(sql);
    }
}
