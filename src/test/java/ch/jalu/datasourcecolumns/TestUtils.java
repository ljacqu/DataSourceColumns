package ch.jalu.datasourcecolumns;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class TestUtils {

    private TestUtils() {
    }

    public static String readToString(InputStream inputStream) {
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            // StandardCharsets.UTF_8.name() > JDK 7
            return result.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

}
