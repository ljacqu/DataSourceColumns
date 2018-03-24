package ch.jalu.datasourcecolumns.demo;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DemoDatabaseInitializer {

    private final Configuration configuration;
    private final Connection connection;

    private DemoDatabaseInitializer(Configuration configuration, Connection connection) {
        this.configuration = configuration;
        this.connection = connection;
    }


    public static Connection createH2InMemoryDatabase(String databaseName, Configuration configuration) {
        try {
            Connection connection = initDatabase(databaseName);
            DemoDatabaseInitializer db = new DemoDatabaseInitializer(configuration, connection);
            db.createTable();
            return connection;
        } catch (Exception e) {
            throw new IllegalStateException("Error while trying to set up test database", e);
        }
    }

    private void createTable() throws SQLException {

        // Initialize database with name column (primary key - may not be skipped)
        String nameColumn = PersonColumns.NAME.resolveName(configuration);
        String sql = "create table tbl ( :name varchar not null, primary key(:name) );"
            .replace(":name", nameColumn);
        connection.prepareStatement(sql).executeUpdate();

        addColumnIfNotSkipped(PersonColumns.LOCATION, "varchar");
        addColumnIfNotSkipped(PersonColumns.AGE, "int4");
        addColumnIfNotSkipped(PersonColumns.WEIGHT, "bigint");
        addColumnIfNotSkipped(PersonColumns.IS_HOME, "bool");
    }

    private void addColumnIfNotSkipped(PersonColumns column, String columnType) throws SQLException {
        if (column.isColumnUsed(configuration)) {
            Statement st = connection.createStatement();
            st.executeUpdate("alter table tbl add column "
                + column.resolveName(configuration) + " " + columnType);
        }
    }

    private static Connection initDatabase(String name) throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
        config.setConnectionTestQuery("VALUES 1");
        config.addDataSourceProperty("URL", "jdbc:h2:mem:" + name);
        config.addDataSourceProperty("user", "sa");
        config.addDataSourceProperty("password", "sa");
        HikariDataSource ds = new HikariDataSource(config);
        return ds.getConnection();
    }
}
