package ch.jalu.datasourcecolumns;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class MainTest {

    @Test
    public void shouldWork() {
        assertThat(Main.v(), equalTo("S"));
    }

}
