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
        // Note "ignorecase=true": H2 does not support `COLLATE NOCASE` for case-insensitive equals queries.
        config.addDataSourceProperty("URL", "jdbc:h2:mem:test;ignorecase=true");
        config.addDataSourceProperty("user", "sa");
        config.addDataSourceProperty("password", "sa");
        HikariDataSource ds = new HikariDataSource(config);
        return ds.getConnection();
    }
}
