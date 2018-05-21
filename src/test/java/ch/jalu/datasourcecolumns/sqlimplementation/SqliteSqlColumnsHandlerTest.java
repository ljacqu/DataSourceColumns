package ch.jalu.datasourcecolumns.sqlimplementation;

import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Test for {@link SqlColumnsHandler}, using an in-memory SQLite database.
 */
public class SqliteSqlColumnsHandlerTest extends AbstractSqlColumnsHandlerTest {

    @Override
    protected ConnectionInfo createConnection() throws SQLException {
        return new ConnectionInfo(
            createDataSource().getConnection());
    }

    protected DataSource createDataSource() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite::memory:");
        return dataSource;
    }

    @Override
    protected boolean hasSupportForDefaultKeyword() {
        // SQLite does not support the DEFAULT keyword
        return false;
    }

    @Override
    protected boolean useNoCaseCollationForIgnoreCasePredicate() {
        return true;
    }
}
