package ch.jalu.datasourcecolumns.sqlimplementation;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * Test for {@link SqlColumnsHandler}, using an in-memory SQLite database with a database pool.
 */
class PooledSqliteSqlColumnsHandlerTestImpl extends SqliteSqlColumnsHandlerTest {

    @Override
    protected ConnectionInfo createConnection() {
        DataSource dataSource = createDataSource();
        HikariConfig config = new HikariConfig();
        config.setDataSource(dataSource);
        config.setConnectionTestQuery("SELECT 1");
        config.setMaximumPoolSize(5);
        HikariDataSource ds = new HikariDataSource(config);
        return new ConnectionInfo(ds);
    }
}
