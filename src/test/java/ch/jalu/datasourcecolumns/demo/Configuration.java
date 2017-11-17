package ch.jalu.datasourcecolumns.demo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

    private final Properties properties;

    private Configuration(Properties properties) {
        this.properties = properties;
    }

    public static Configuration loadConfig(String propertiesPath) {
        Properties properties = new Properties();

        try (InputStream propertiesFile = Configuration.class.getResourceAsStream(propertiesPath)) {
            properties.load(propertiesFile);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return new Configuration(properties);
    }

    public String getColumnName(String columnId) {
        return properties.getProperty("column." + columnId + ".name");
    }

    public boolean shouldBeSkipped(String columnId) {
        String skipValue = properties.getProperty("column." + columnId + ".skip");
        return "true".equals(skipValue);
    }
}
