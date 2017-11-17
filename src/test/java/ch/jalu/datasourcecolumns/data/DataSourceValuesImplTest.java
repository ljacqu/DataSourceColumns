package ch.jalu.datasourcecolumns.data;

import ch.jalu.datasourcecolumns.SampleColumns;
import org.junit.Test;

import static ch.jalu.datasourcecolumns.TestUtils.expectException;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link DataSourceValuesImpl}.
 */
public class DataSourceValuesImplTest {

    @Test
    public void shouldStoreValues() {
        // given
        DataSourceValuesImpl values = new DataSourceValuesImpl();

        // when
        values.put(SampleColumns.IS_ACTIVE, 5);
        values.put(SampleColumns.NAME, "test");

        // then
        assertThat(values.get(SampleColumns.IS_ACTIVE), equalTo(5));
        assertThat(values.get(SampleColumns.NAME), equalTo("test"));
        expectException(IllegalArgumentException.class, () -> values.get(SampleColumns.IP));
    }

    @Test
    public void shouldRejectValueOfInvalidType() {
        // given
        DataSourceValuesImpl values = new DataSourceValuesImpl();

        // when
        Exception exception = expectException(IllegalArgumentException.class,
            () -> values.put(SampleColumns.LAST_LOGIN, "test"));

        // then
        assertThat(exception.getMessage(), containsString("does not have the correct type"));
    }
}