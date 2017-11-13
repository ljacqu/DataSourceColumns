package ch.jalu.datasourcecolumns.sqlimplementation;

import ch.jalu.datasourcecolumns.SampleColumns;
import ch.jalu.datasourcecolumns.SampleContext;
import ch.jalu.datasourcecolumns.data.DataSourceValues;
import ch.jalu.datasourcecolumns.data.UpdateValues;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static ch.jalu.datasourcecolumns.data.UpdateValues.with;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link SqlColumnsHandler}, using an in-memory H2 database.
 */
public class H2SqlColumnsHandlerTest extends AbstractSqlColumnsHandlerTest {

    @Override
    protected Connection createConnection() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
        config.setConnectionTestQuery("VALUES 1");
        config.addDataSourceProperty("URL", "jdbc:h2:mem:test");
        config.addDataSourceProperty("user", "sa");
        config.addDataSourceProperty("password", "sa");
        HikariDataSource ds = new HikariDataSource(config);
        return ds.getConnection();
    }

    @Test
    public void shouldPerformMultiUpdateWithDefaultValueForNull() throws SQLException {
        // given
        context.setUseDefaults(true, true);

        // when
        boolean result = handler.update(6,
            with(SampleColumns.IS_LOCKED, 1)
                .and(SampleColumns.LAST_LOGIN, null)
                .and(SampleColumns.IS_ACTIVE, null)
                .and(SampleColumns.EMAIL, "snow@example.com").build());

        // then
        assertThat(result, equalTo(true));
        assertThat(handler.retrieve(6, SampleColumns.IS_LOCKED).getValue(), equalTo(1));
        assertThat(handler.retrieve(6, SampleColumns.LAST_LOGIN).getValue(), equalTo(LAST_LOGIN_DEFAULT));
        assertThat(handler.retrieve(6, SampleColumns.IS_ACTIVE).getValue(), equalTo(IS_ACTIVE_DEFAULT));
        assertThat(handler.retrieve(6, SampleColumns.EMAIL).getValue(), equalTo("snow@example.com"));
    }

    @Test
    public void shouldInsertUsingDefaultKeywordForNullValues() throws SQLException {
        // given
        context.setUseDefaults(true, false);
        UpdateValues<SampleContext> values =
            with(SampleColumns.ID, 414)
                .and(SampleColumns.NAME, "Oscar")
                .and(SampleColumns.IS_LOCKED, 1)
                .and(SampleColumns.IS_ACTIVE, null)
                .and(SampleColumns.EMAIL, "value@example.org")
                .and(SampleColumns.LAST_LOGIN, null)
                .build();

        // when
        boolean result = handler.insert(values);

        // then
        assertThat(result, equalTo(true));
        DataSourceValues retrievedValues = handler.retrieve(414,
            SampleColumns.NAME, SampleColumns.IS_LOCKED, SampleColumns.IS_ACTIVE, COL_EMAIL, COL_LAST_LOGIN);
        assertThat(retrievedValues.get(SampleColumns.NAME), equalTo("Oscar"));
        assertThat(retrievedValues.get(SampleColumns.IS_LOCKED), equalTo(1));
        assertThat(retrievedValues.get(SampleColumns.IS_ACTIVE), equalTo(IS_ACTIVE_DEFAULT));
        assertThat(retrievedValues.get(COL_EMAIL), equalTo("value@example.org"));
        assertThat(retrievedValues.get(COL_LAST_LOGIN), nullValue());
    }

    @Test
    public void shouldPerformUpdateWithDefaultForNullValue() throws SQLException {
        // given
        context.setUseDefaults(true, true);

        // when
        boolean result1 = handler.update(8, SampleColumns.LAST_LOGIN, (Long) null);
        boolean result2 = handler.update(8, SampleColumns.IS_ACTIVE, (Integer) null);

        // then
        assertThat(result1, equalTo(true));
        assertThat(handler.retrieve(8, SampleColumns.LAST_LOGIN).getValue(), equalTo(LAST_LOGIN_DEFAULT));
        assertThat(result2, equalTo(true));
        assertThat(handler.retrieve(8, SampleColumns.IS_ACTIVE).getValue(), equalTo(IS_ACTIVE_DEFAULT));
    }
}
