package ch.jalu.datasourcecolumns.sqlimplementation;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Test for {@link SqlColumnsHandler}, using an in-memory H2 database.
 */
public class H2SqlColumnsHandlerTest extends AbstractSqlColumnsHandlerTest {

    @Override
    protected ConnectionInfo createConnection() throws SQLException {
        DataSource dataSource = createDataSource();
        return new ConnectionInfo(dataSource.getConnection());
    }

    protected DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        // Note "ignorecase=true": H2 does not support `COLLATE NOCASE` for case-insensitive equals queries.
        dataSource.setURL("jdbc:h2:mem:test;ignorecase=true");
        return dataSource;
    }
}
