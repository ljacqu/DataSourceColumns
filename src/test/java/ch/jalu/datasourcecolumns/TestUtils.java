package ch.jalu.datasourcecolumns;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class TestUtils {

    private TestUtils() {
    }

    public static Path getResourceFile(String name) {
        URL resourceUrl = TestUtils.class.getResource(name);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Unknown resource with path '" + name + "'");
        }
        try {
            return Paths.get(resourceUrl.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Failed to get resource with path '" + name + "'", e);
        }
    }
}
