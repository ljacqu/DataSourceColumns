package ch.jalu.datasourcecolumns.sqlimplementation;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Test for {@link SqlColumnsHandler}, using an in-memory SQLite database.
 */
public class SqliteSqlColumnsHandlerTest extends AbstractSqlColumnsHandlerTest {

    @Override
    protected Connection createConnection() throws Exception {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite::memory:");
    }

    @Override
    protected boolean hasSupportForDefaultKeyword() {
        // SQLite does not support the DEFAULT keyword
        return false;
    }
}
