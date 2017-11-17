package ch.jalu.datasourcecolumns.data;

import ch.jalu.datasourcecolumns.SampleColumns;
import ch.jalu.datasourcecolumns.SampleContext;
import org.junit.Test;

import static ch.jalu.datasourcecolumns.TestUtils.expectException;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link UpdateValuesTest}.
 */
public class UpdateValuesTest {

    @Test
    public void shouldThrowForUnregisteredValue() {
        // given
        UpdateValues<SampleContext> values = UpdateValues
            .with(SampleColumns.IP, "124.4.4")
            .and(SampleColumns.LAST_LOGIN, null).build();

        // when / then
        assertThat(values.get(SampleColumns.IP), equalTo("124.4.4"));
        assertThat(values.get(SampleColumns.LAST_LOGIN), nullValue());
        expectException(IllegalArgumentException.class, () -> values.get(SampleColumns.IS_ACTIVE));
    }
}