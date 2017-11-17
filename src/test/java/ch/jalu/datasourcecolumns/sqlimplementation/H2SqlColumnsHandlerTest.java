package ch.jalu.datasourcecolumns.sqlimplementation;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;

/**
 * Test for {@link SqlColumnsHandler}, using an in-memory H2 database.
 */
public class H2SqlColumnsHandlerTest extends AbstractSqlColumnsHandlerTest {

    @Override
    protected Connection createConnection() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
        config.setConnectionTestQuery("VALUES 1");
        config.addDataSourceProperty("URL", "jdbc:h2:mem:test");
        config.addDataSourceProperty("user", "sa");
        config.addDataSourceProperty("password", "sa");
        HikariDataSource ds = new HikariDataSource(config);
        return ds.getConnection();
    }
}
