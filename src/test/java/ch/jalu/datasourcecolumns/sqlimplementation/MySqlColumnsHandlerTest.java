package ch.jalu.datasourcecolumns.sqlimplementation;

import org.junit.Ignore;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Test for {@link SqlColumnsHandler}, using an external MySQL database.
 */
@Ignore
public class MySqlColumnsHandlerTest extends AbstractSqlColumnsHandlerTest {

    @Override
    protected ConnectionInfo createConnection() throws Exception {
        Connection connection = DriverManager.getConnection(createJdbcUrl());
        return new ConnectionInfo(connection);
    }

    protected String createJdbcUrl() throws IOException {
        Properties properties = loadProperties();
        String host = properties.getProperty("mysql.host");
        String port = getPort(properties);
        String database = properties.getProperty("mysql.database");
        String user = properties.getProperty("mysql.username");
        String pass = properties.getProperty("mysql.password");

        return "jdbc:mysql://" + host + port + "/" + database
            + "?user=" + user + "&password=" + pass;
    }

    private String getPort(Properties properties) {
        String port = properties.getProperty("mysql.port");
        if (port == null) {
            port = "";
        } else if (!port.isEmpty()) {
            port = ":" + port;
        }
        return port;
    }

    private Properties loadProperties() throws IOException {
        try (InputStream propertiesFile = getClass().getResourceAsStream("/mysql_connection.properties")) {
            Properties properties = new Properties();
            properties.load(propertiesFile);
            return properties;
        }
    }
}
