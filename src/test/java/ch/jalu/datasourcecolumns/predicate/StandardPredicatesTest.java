package ch.jalu.datasourcecolumns.predicate;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link StandardPredicates}.
 */
public class StandardPredicatesTest {

    @Test
    public void shouldHaveAProtectedConstructor() throws ReflectiveOperationException {
        // given
        Constructor<?>[] constructors = StandardPredicates.class.getDeclaredConstructors();

        // when / then
        assertThat(constructors, arrayWithSize(1));
        assertThat(Modifier.isProtected(constructors[0].getModifiers()), equalTo(true));
        constructors[0].newInstance(); // dirty trick for coverage
    }
}