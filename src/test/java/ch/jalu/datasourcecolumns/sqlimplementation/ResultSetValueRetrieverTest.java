package ch.jalu.datasourcecolumns.sqlimplementation;

import ch.jalu.datasourcecolumns.Column;
import ch.jalu.datasourcecolumns.ColumnType;
import ch.jalu.datasourcecolumns.SampleColumns;
import ch.jalu.datasourcecolumns.SampleContext;
import ch.jalu.datasourcecolumns.StandardTypes;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static ch.jalu.datasourcecolumns.TestUtils.expectException;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link ResultSetValueRetriever}.
 */
public class ResultSetValueRetrieverTest {

    private ResultSetValueRetriever<SampleContext> valueRetriever = createRetriever();

    @Test
    public void shouldRetrieveIntegerValues() throws SQLException {
        // given
        ResultSet rs = mock(ResultSet.class);
        given(rs.getInt("id")).willReturn(4242);
        given(rs.getInt("is_locked")).willReturn(0);
        given(rs.wasNull()).willReturn(true);

        // when
        Integer id = valueRetriever.get(rs, SampleColumns.ID);
        Integer isLocked = valueRetriever.get(rs, SampleColumns.IS_LOCKED);

        // then
        assertThat(id, equalTo(4242));
        assertThat(isLocked, nullValue());
    }

    @Test
    public void shouldRetrieveStringValues() throws SQLException {
        // given
        ResultSet rs = mock(ResultSet.class);
        given(rs.getString("username")).willReturn("Test3");
        given(rs.getString("ip")).willReturn(null);

        // when
        String name = valueRetriever.get(rs, SampleColumns.NAME);
        String ip = valueRetriever.get(rs, SampleColumns.IP);

        // then
        assertThat(name, equalTo("Test3"));
        assertThat(ip, nullValue());
    }

    @Test
    public void shouldRetrieveLongValues() throws SQLException {
        // given
        ResultSet rs = mock(ResultSet.class);
        given(rs.getLong("last_login")).willReturn(355L).willReturn(0L);
        given(rs.wasNull()).willReturn(true);

        // when
        Long lastLogin1 = valueRetriever.get(rs, SampleColumns.LAST_LOGIN);
        Long lastLogin2 = valueRetriever.get(rs, SampleColumns.LAST_LOGIN);

        // then
        assertThat(lastLogin1, equalTo(355L));
        assertThat(lastLogin2, nullValue());
    }

    @Test
    public void shouldRetrieveBooleanValues() throws SQLException {
        // given
        Column<Boolean, SampleContext> col = new BooleanColumn();
        ResultSet rs = mock(ResultSet.class);
        given(rs.getBoolean("is_admin")).willReturn(true).willReturn(false);
        given(rs.wasNull()).willReturn(true);

        // when
        Boolean isAdmin1 = valueRetriever.get(rs, col);
        Boolean isAdmin2 = valueRetriever.get(rs, col);

        // then
        assertThat(isAdmin1, equalTo(true));
        assertThat(isAdmin2, nullValue());
    }

    @Test
    public void shouldThrowForUnknownType() {
        // given
        ColumnType<Object> type = mock(ColumnType.class);

        // when
        IllegalArgumentException ex = expectException(IllegalArgumentException.class,
            () -> valueRetriever.createResultSetGetter(type));

        // then
        assertThat(ex.getMessage(), containsString("Unhandled type"));
    }

    private ResultSetValueRetriever<SampleContext> createRetriever() {
        SampleContext context = new SampleContext();
        return new ResultSetValueRetriever<>(context);
    }

    private static final class BooleanColumn implements Column<Boolean, SampleContext> {

        @Override
        public String resolveName(SampleContext context) {
            return "is_admin";
        }

        @Override
        public ColumnType<Boolean> getType() {
            return StandardTypes.BOOLEAN;
        }

        @Override
        public boolean isColumnUsed(SampleContext context) {
            return true;
        }

        @Override
        public boolean useDefaultForNullValue(SampleContext context) {
            return false;
        }
    }
}