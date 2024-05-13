package ch.jalu.datasourcecolumns.sqlimplementation;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;

/**
 * Test for {@link SqlColumnsHandler}, using an external MySQL database with a pooled connection.
 */
@Disabled
class PooledMySqlColumnsHandlerTestImpl extends MySqlColumnsHandlerTest {

    @Override
    protected ConnectionInfo createConnection() throws IOException {
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName(MysqlDataSource.class.getName());
        config.setConnectionTestQuery("SELECT 1");
        config.setMaximumPoolSize(5);
        config.addDataSourceProperty("URL", createJdbcUrl());
        HikariDataSource ds = new HikariDataSource(config);
        return new ConnectionInfo(ds);
    }
}
