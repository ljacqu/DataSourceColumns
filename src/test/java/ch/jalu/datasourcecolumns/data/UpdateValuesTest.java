package ch.jalu.datasourcecolumns.data;

import ch.jalu.datasourcecolumns.SampleColumns;
import ch.jalu.datasourcecolumns.SampleContext;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link UpdateValuesTest}.
 */
class UpdateValuesTest {

    @Test
    void shouldThrowForUnregisteredValue() {
        // given
        UpdateValues<SampleContext> values = UpdateValues
            .with(SampleColumns.IP, "124.4.4")
            .and(SampleColumns.LAST_LOGIN, null).build();

        // when / then
        assertThat(values.get(SampleColumns.IP), equalTo("124.4.4"));
        assertThat(values.get(SampleColumns.LAST_LOGIN), nullValue());
        assertThrows(IllegalArgumentException.class, () -> values.get(SampleColumns.IS_ACTIVE));
    }
}