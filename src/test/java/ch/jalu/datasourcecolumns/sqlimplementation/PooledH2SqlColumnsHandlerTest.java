package ch.jalu.datasourcecolumns.sqlimplementation;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Test for {@link SqlColumnsHandler}, using an in-memory H2 database with a database pool.
 */
class PooledH2SqlColumnsHandlerTest extends H2SqlColumnsHandlerTest {

    @Override
    protected ConnectionInfo createConnection() {
        HikariConfig config = new HikariConfig();
        config.setDataSource(createDataSource());
        config.setConnectionTestQuery("SELECT 1");
        config.setMaximumPoolSize(5);
        HikariDataSource ds = new HikariDataSource(config);
        return new ConnectionInfo(ds);
    }
}
