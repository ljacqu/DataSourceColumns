package ch.jalu.datasourcecolumns;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.fail;

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

    public static <X extends Exception> X expectException(Class<X> exceptionClass, ThrowingRunnable runnable) {
        try {
            runnable.run();
            fail("Expected exception to be thrown");
        } catch (Exception e) {
            if (exceptionClass.isInstance(e)) {
                return exceptionClass.cast(e);
            } else {
                e.printStackTrace();
                fail("Expected exception of type '" + exceptionClass.getSimpleName()
                    + "', but got exception of type '" + e.getClass().getSimpleName() + "'");
            }
        }
        throw new IllegalStateException(); // should never happen
    }

    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}
