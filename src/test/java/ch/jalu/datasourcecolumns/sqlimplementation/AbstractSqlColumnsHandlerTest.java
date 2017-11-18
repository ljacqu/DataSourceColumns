package ch.jalu.datasourcecolumns.sqlimplementation;

import ch.jalu.datasourcecolumns.Column;
import ch.jalu.datasourcecolumns.ColumnType;
import ch.jalu.datasourcecolumns.SampleColumns;
import ch.jalu.datasourcecolumns.SampleContext;
import ch.jalu.datasourcecolumns.SampleDependent;
import ch.jalu.datasourcecolumns.StandardTypes;
import ch.jalu.datasourcecolumns.TestUtils;
import ch.jalu.datasourcecolumns.data.DataSourceValue;
import ch.jalu.datasourcecolumns.data.DataSourceValues;
import ch.jalu.datasourcecolumns.data.UpdateValues;
import ch.jalu.datasourcecolumns.predicate.Predicate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ch.jalu.datasourcecolumns.TestUtils.expectException;
import static ch.jalu.datasourcecolumns.data.UpdateValues.with;
import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.eq;
import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.eqIgnoreCase;
import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.greaterThan;
import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.greaterThanEquals;
import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.isNotNull;
import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.isNull;
import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.notEq;
import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.notEqIgnoreCase;
import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.or;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

/**
 * Integration test for {@link SqlColumnsHandler}.
 */
public abstract class AbstractSqlColumnsHandlerTest {

    private static final String TABLE_NAME = "testingdata";
    private static final String ID_COLUMN = "id";

    private static final long LAST_LOGIN_DEFAULT = -123L;
    private static final int IS_ACTIVE_DEFAULT = 3;

    // Columns that are not declared as empty for retrieval in test assertions
    private static final ColumnImpl<String> COL_EMAIL = new ColumnImpl<>("email", StandardTypes.STRING);
    private static final ColumnImpl<Long> COL_LAST_LOGIN = new ColumnImpl<>("last_login", StandardTypes.LONG);
    private static final ColumnImpl<Integer> COL_IS_LOCKED = new ColumnImpl<>("is_locked", StandardTypes.INTEGER);

    private Connection connection;
    private SqlColumnsHandler<SampleContext, Integer> handler;
    private SampleContext context;

    @Before
    public void setUpConnection() throws Exception {
        connection = createConnection();

        InputStream is = getClass().getClassLoader().getResourceAsStream("sample-database.sql");
        // We can only run one statement per Statement.execute() so we split
        // the string by ";\n" as to get the individual statements
        String[] sqlInitialize = TestUtils.readToString(is).split(";(\\r?)\\n");

        try (Statement st = connection.createStatement()) {
            st.execute("DROP TABLE IF EXISTS " + TABLE_NAME);
            for (String statement : sqlInitialize) {
                st.execute(statement);
            }
        }

        context = new SampleContext();
        if (useNoCaseCollationForIgnoreCasePredicate()) {
            handler = new SqlColumnsHandler<>(connection::prepareStatement, context, TABLE_NAME, ID_COLUMN,
                new ResultSetValueRetriever<>(context), new PredicateSqlGenerator<>(context, true));
        } else {
            handler = new SqlColumnsHandler<>(connection, context, TABLE_NAME, ID_COLUMN);
        }
    }

    @After
    public void tearDownConnection() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    protected abstract Connection createConnection() throws Exception;

    protected boolean hasSupportForDefaultKeyword() {
        return true;
    }

    protected boolean useNoCaseCollationForIgnoreCasePredicate() {
        return false;
    }

    @Test
    public void shouldRetrieveSingleValue() throws SQLException {
        // given / when
        DataSourceValue<String> alexIp = handler.retrieve(1, SampleColumns.IP);
        DataSourceValue<String> emilyIp = handler.retrieve(5, SampleColumns.IP);
        DataSourceValue<String> nonExistentIp = handler.retrieve(222, SampleColumns.IP);

        // then
        assertThat(alexIp.rowExists(), equalTo(true));
        assertThat(alexIp.getValue(), equalTo("111.111.111.111"));
        assertThat(emilyIp.rowExists(), equalTo(true));
        assertThat(emilyIp.getValue(), nullValue());
        assertThat(nonExistentIp.rowExists(), equalTo(false));
        assertThat(nonExistentIp.getValue(), nullValue());
    }

    @Test
    public void shouldHandleEmptyColumnRetrieval() throws SQLException {
        // given
        context.setEmptyOptions(true, false, false);

        // when
        DataSourceValue<String> result1 = handler.retrieve(2, SampleColumns.EMAIL);
        DataSourceValue<String> result2 = handler.retrieve(777, SampleColumns.EMAIL);

        // then
        assertThat(result1.rowExists(), equalTo(true));
        assertThat(result1.getValue(), nullValue());
        assertThat(result2.rowExists(), equalTo(false));
        assertThat(result2.getValue(), nullValue());
    }

    @Test
    public void shouldRetrieveMultipleValues() throws SQLException {
        // given
        SampleColumns<?>[] columns = { SampleColumns.NAME, SampleColumns.IS_LOCKED, SampleColumns.LAST_LOGIN };

        // when
        DataSourceValues hansValues = handler.retrieve(8, columns);
        DataSourceValues finnValues = handler.retrieve(6, columns);
        DataSourceValues nonExistent = handler.retrieve(-5, columns);

        // then
        assertThat(hansValues.get(SampleColumns.NAME), equalTo("Hans"));
        assertThat(hansValues.get(SampleColumns.IS_LOCKED), equalTo(1));
        assertThat(hansValues.get(SampleColumns.LAST_LOGIN), equalTo(77665544L));
        verifyThrowsException(() -> hansValues.get(SampleColumns.ID));

        assertThat(finnValues.get(SampleColumns.NAME), equalTo("Finn"));
        assertThat(finnValues.get(SampleColumns.IS_LOCKED), equalTo(0));
        assertThat(finnValues.get(SampleColumns.LAST_LOGIN), nullValue());
        verifyThrowsException(() -> finnValues.get(SampleColumns.IS_ACTIVE));

        assertThat(nonExistent.rowExists(), equalTo(false));
        verifyThrowsException(() -> nonExistent.get(SampleColumns.NAME));
    }

    @Test
    public void shouldHandleRetrievalOfMultipleValuesIncludingEmpty() throws SQLException {
        // given
        context.setEmptyOptions(true, false, true);
        SampleColumns<?>[] columns = { SampleColumns.NAME, SampleColumns.IS_LOCKED, SampleColumns.LAST_LOGIN };

        // when
        DataSourceValues hansValues = handler.retrieve(8, columns);
        DataSourceValues finnValues = handler.retrieve(6, columns);
        DataSourceValues nonExistent = handler.retrieve(-5, columns);

        // then
        assertThat(hansValues.get(SampleColumns.NAME), equalTo("Hans"));
        assertThat(hansValues.get(SampleColumns.IS_LOCKED), equalTo(1));
        assertThat(hansValues.get(SampleColumns.LAST_LOGIN), nullValue());
        verifyThrowsException(() -> hansValues.get(SampleColumns.ID));

        assertThat(finnValues.get(SampleColumns.NAME), equalTo("Finn"));
        assertThat(finnValues.get(SampleColumns.IS_LOCKED), equalTo(0));
        assertThat(finnValues.get(SampleColumns.LAST_LOGIN), nullValue());
        verifyThrowsException(() -> finnValues.get(SampleColumns.IS_ACTIVE));

        assertThat(nonExistent.rowExists(), equalTo(false));
        verifyThrowsException(() -> nonExistent.get(SampleColumns.LAST_LOGIN));
    }

    @Test
    public void shouldRetrieveMultipleAllEmptyColumnsSuccessfully() throws SQLException {
        // given
        context.setEmptyOptions(true, false, true);
        SampleColumns<?>[] columns = { SampleColumns.EMAIL, SampleColumns.LAST_LOGIN };

        // when
        DataSourceValues hansValues = handler.retrieve(8, columns);
        DataSourceValues nonExistent = handler.retrieve(-5, columns);

        // then
        assertThat(hansValues.rowExists(), equalTo(true));
        assertThat(hansValues.get(SampleColumns.EMAIL), nullValue());
        assertThat(hansValues.get(SampleColumns.LAST_LOGIN), nullValue());
        verifyThrowsException(() -> hansValues.get(SampleColumns.ID));

        assertThat(nonExistent.rowExists(), equalTo(false));
        verifyThrowsException(() -> nonExistent.get(SampleColumns.LAST_LOGIN));
    }

    @Test
    public void shouldRetrieveValueOfRowsMatchingPredicate() throws SQLException {
        // given
        Predicate<SampleContext> predicate = or(eq(SampleColumns.IP, "22.22.22.22"), eq(SampleColumns.IP, "111.111.111.111"))
            .and(isNotNull(SampleColumns.EMAIL));

        // when
        List<String> matchingNames = handler.retrieve(predicate, SampleColumns.NAME);

        // then
        assertThat(matchingNames, containsInAnyOrder("Brett", "Cody", "Finn", "Igor", "Keane"));
    }

    @Test
    public void shouldHandleOptionalColumnForPredicateRetrieval() throws SQLException {
        // given
        context.setEmptyOptions(true, true, false);
        Predicate<SampleContext> predicate = greaterThan(SampleColumns.LAST_LOGIN, 123456L);

        // when
        List<Integer> result = handler.retrieve(predicate, SampleColumns.IS_LOCKED);

        // then
        assertThat(result, contains(null, null, null, null, null, null));
    }

    @Test
    public void shouldReturnEmptyListForNoRowsFulfillingPredicate() throws SQLException {
        // given
        Predicate<SampleContext> predicate = eq(SampleColumns.IP, "111.111.111.111")
            .and(eqIgnoreCase(SampleColumns.EMAIL, "other@test.tld"));

        // when
        List<Long> result = handler.retrieve(predicate, SampleColumns.LAST_LOGIN);

        // then
        assertThat(result, empty());
    }

    @Test
    public void shouldRetrieveValuesOfRowsMatchingPredicate() throws SQLException {
        // given
        Predicate<SampleContext> predicate = or(eq(SampleColumns.IP, "22.22.22.22"), eq(SampleColumns.IP, "111.111.111.111"))
            .and(isNotNull(SampleColumns.EMAIL));

        // when
        List<DataSourceValues> result = handler.retrieve(predicate, SampleColumns.NAME, SampleColumns.LAST_LOGIN);

        // then
        assertThat(result, hasSize(5));

        List<String> names = new ArrayList<>(5);
        List<Long> lastLogins = new ArrayList<>(5);
        result.forEach(r -> {
            names.add(r.get(SampleColumns.NAME));
            lastLogins.add(r.get(SampleColumns.LAST_LOGIN));
        });

        assertThat(names, containsInAnyOrder("Brett", "Cody", "Finn", "Igor", "Keane"));
        assertThat(lastLogins, containsInAnyOrder(123456L, 888888L, null, 725124L, 888888L));
    }

    @Test
    public void shouldReturnEmptyListForNoRowsMatchingPredicate() throws SQLException {
        // given
        Predicate<SampleContext> predicate = greaterThan(SampleColumns.ID, 20);

        // when
        List<DataSourceValues> result = handler.retrieve(predicate, SampleColumns.ID, SampleColumns.EMAIL, SampleColumns.IP);

        // then
        assertThat(result, empty());
    }

    @Test
    public void shouldPerformSingleValueUpdate() throws SQLException {
        // given / when
        boolean result1 = handler.update(1, SampleColumns.EMAIL, "mailForAlex@example.org");
        boolean result2 = handler.update(2, SampleColumns.EMAIL, (String) null);
        boolean result3 = handler.update(999, SampleColumns.EMAIL, "");

        // then
        assertThat(result1, equalTo(true));
        assertThat(handler.retrieve(1, SampleColumns.EMAIL).getValue(), equalTo("mailForAlex@example.org"));
        assertThat(result2, equalTo(true));
        assertThat(handler.retrieve(2, SampleColumns.EMAIL).getValue(), nullValue());
        assertThat(result3, equalTo(false));
    }

    @Test
    public void shouldHandleSingleValueUpdateWithEmptyColumn() throws SQLException {
        // given
        context.setEmptyOptions(true, false, false);

        // when
        boolean result1 = handler.update(2, SampleColumns.EMAIL, "mailForAlex@example.org");
        boolean result2 = handler.update(999, SampleColumns.EMAIL, "");

        // then
        assertThat(result1, equalTo(true));
        // check that email was not updated
        assertThat(handler.retrieve(2, COL_EMAIL).getValue(), equalTo("test@example.com"));

        assertThat(result2, equalTo(true));
    }

    @Test
    public void shouldPerformMultiValueUpdate() throws SQLException {
        // given / when
        boolean result1 = handler.update(9,
            with(SampleColumns.IS_LOCKED, 1)
            .and(SampleColumns.EMAIL, null)
            .and(SampleColumns.LAST_LOGIN, 1258L).build());
        boolean result2 = handler.update(12,
            with(SampleColumns.IS_LOCKED, 0)
            .and(SampleColumns.EMAIL, "mymail@test.tld")
            .and(SampleColumns.LAST_LOGIN, null).build());
        boolean result3 = handler.update(-9999,
            with(SampleColumns.IS_LOCKED, 0)
            .and(SampleColumns.EMAIL, "mymail@test.tld")
            .and(SampleColumns.LAST_LOGIN, null).build());

        // then
        assertThat(result1, equalTo(true));
        DataSourceValues igorValues = handler.retrieve(9, SampleColumns.IS_LOCKED, SampleColumns.EMAIL, SampleColumns.LAST_LOGIN);
        assertThat(igorValues.get(SampleColumns.IS_LOCKED), equalTo(1));
        assertThat(igorValues.get(SampleColumns.EMAIL), equalTo(null));
        assertThat(igorValues.get(SampleColumns.LAST_LOGIN), equalTo(1258L));

        assertThat(result2, equalTo(true));
        DataSourceValues louisValues = handler.retrieve(12, SampleColumns.IS_LOCKED, SampleColumns.EMAIL, SampleColumns.LAST_LOGIN);
        assertThat(louisValues.get(SampleColumns.IS_LOCKED), equalTo(0));
        assertThat(louisValues.get(SampleColumns.EMAIL), equalTo("mymail@test.tld"));
        assertThat(louisValues.get(SampleColumns.LAST_LOGIN), equalTo(null));

        assertThat(result3, equalTo(false));
    }

    @Test
    public void shouldPerformMultiUpdateWithEmptyColumns() throws SQLException {
        // given
        context.setEmptyOptions(true, false, true);

        // when
        boolean result1 = handler.update(9,
            with(SampleColumns.IS_LOCKED, 1)
            .and(SampleColumns.EMAIL, null)
            .and(SampleColumns.LAST_LOGIN, 1258L).build());
        boolean result2 = handler.update(-9999,
            with(SampleColumns.IS_LOCKED, 0)
            .and(SampleColumns.EMAIL, "mymail@test.tld")
            .and(SampleColumns.LAST_LOGIN, null).build());

        // then
        assertThat(result1, equalTo(true));
        assertThat(handler.retrieve(9, SampleColumns.IS_LOCKED).getValue(), equalTo(1));
        // assert email / last login unchanged
        assertThat(handler.retrieve(9, COL_EMAIL).getValue(), equalTo("other@test.tld"));
        assertThat(handler.retrieve(9, COL_LAST_LOGIN).getValue(), equalTo(725124L));

        assertThat(result2, equalTo(false));
    }

    @Test
    public void shouldPerformMultiUpdateWithAllEmptyColumns() throws SQLException {
        // given
        context.setEmptyOptions(true, true, true);

        // when
        boolean result1 = handler.update(9,
            with(SampleColumns.IS_LOCKED, 1)
            .and(SampleColumns.EMAIL, null)
            .and(SampleColumns.LAST_LOGIN, 1258L).build());
        boolean result2 = handler.update(-9999,
            with(SampleColumns.IS_LOCKED, 0)
            .and(SampleColumns.EMAIL, "mymail@test.tld")
            .and(SampleColumns.LAST_LOGIN, null).build());

        // then
        assertThat(result1, equalTo(false));
        // assert unchanged
        assertThat(handler.retrieve(9, COL_IS_LOCKED).getValue(), equalTo(0));
        assertThat(handler.retrieve(9, COL_EMAIL).getValue(), equalTo("other@test.tld"));
        assertThat(handler.retrieve(9, COL_LAST_LOGIN).getValue(), equalTo(725124L));

        assertThat(result2, equalTo(false));
    }

    @Test
    public void shouldUpdateWithDependentObject() throws SQLException {
        // given
        context.setEmptyOptions(true, false, false);
        SampleDependent dependent = new SampleDependent();
        dependent.setName("New name");
        dependent.setEmail("newmail@example.com");
        dependent.setLastLogin(2305982L);
        dependent.setIp(null);

        // when
        boolean result = handler.update(9, dependent,
            SampleColumns.NAME, SampleColumns.EMAIL, SampleColumns.LAST_LOGIN, SampleColumns.IP);

        // then
        assertThat(result, equalTo(true));
        assertThat(handler.retrieve(9, SampleColumns.NAME).getValue(), equalTo(dependent.getName()));
        assertThat(handler.retrieve(9, COL_EMAIL).getValue(), equalTo("other@test.tld"));
        assertThat(handler.retrieve(9, SampleColumns.LAST_LOGIN).getValue(), equalTo(2305982L));
        assertThat(handler.retrieve(9, SampleColumns.IP).getValue(), nullValue());
    }

    @Test
    public void shouldPerformUpdateWithDefaultForNullValue() throws SQLException {
        assumeTrue(hasSupportForDefaultKeyword());

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

    @Test
    public void shouldPerformMultiUpdateWithDefaultValueForNull() throws SQLException {
        assumeTrue(hasSupportForDefaultKeyword());

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
    public void shouldInsertValues() throws SQLException {
        // given
        UpdateValues<SampleContext> values =
            with(SampleColumns.ID, 414)
            .and(SampleColumns.NAME, "Oliver")
            .and(SampleColumns.IS_LOCKED, 0)
            .and(SampleColumns.IS_ACTIVE, 1)
            .and(SampleColumns.LAST_LOGIN, 555L)
            .build();

        // when
        boolean result = handler.insert(values);

        // then
        assertThat(result, equalTo(true));
        DataSourceValues retrievedValues = handler.retrieve(414,
            SampleColumns.NAME, SampleColumns.LAST_LOGIN, SampleColumns.IS_ACTIVE);
        assertThat(retrievedValues.get(SampleColumns.NAME), equalTo("Oliver"));
        assertThat(retrievedValues.get(SampleColumns.IS_ACTIVE), equalTo(1));
        assertThat(retrievedValues.get(SampleColumns.LAST_LOGIN), equalTo(555L));
    }

    @Test
    public void shouldHandleInsertWithEmptyColumns() throws SQLException {
        // given
        context.setEmptyOptions(true, false, true);
        UpdateValues<SampleContext> values =
            with(SampleColumns.ID, 414)
            .and(SampleColumns.NAME, "Oscar")
            .and(SampleColumns.IS_LOCKED, 1)
            .and(SampleColumns.IS_ACTIVE, 1)
            .and(SampleColumns.EMAIL, "value@example.org")
            .and(SampleColumns.LAST_LOGIN, 555L)
            .build();

        // when
        boolean result = handler.insert(values);

        // then
        assertThat(result, equalTo(true));
        DataSourceValues retrievedValues = handler.retrieve(414,
            SampleColumns.NAME, SampleColumns.IS_LOCKED, SampleColumns.IS_ACTIVE, COL_EMAIL, COL_LAST_LOGIN);
        assertThat(retrievedValues.get(SampleColumns.NAME), equalTo("Oscar"));
        assertThat(retrievedValues.get(SampleColumns.IS_LOCKED), equalTo(1));
        assertThat(retrievedValues.get(SampleColumns.IS_ACTIVE), equalTo(1));
        assertThat(retrievedValues.get(COL_EMAIL), nullValue());
        assertThat(retrievedValues.get(COL_LAST_LOGIN), equalTo(LAST_LOGIN_DEFAULT));
    }

    @Test
    public void shouldThrowExceptionForInsertWithNoNonEmptyColumns() throws SQLException {
        // given
        context.setEmptyOptions(true, true, false);
        UpdateValues<SampleContext> values =
            with(SampleColumns.EMAIL, "test@example.com")
            .and(SampleColumns.IS_LOCKED, 0)
            .build();

        // when
        IllegalStateException ex = expectException(IllegalStateException.class,
            () -> handler.insert(values));

        // then
        assertThat(ex.getMessage(), startsWith("Cannot perform insert when all columns are empty"));
    }

    @Test
    public void shouldPerformInsertWithDependentObject() throws SQLException {
        // given
        context.setEmptyOptions(true, false, false);
        SampleDependent dependent = new SampleDependent();
        dependent.setId(155);
        dependent.setName("Jason");
        dependent.setEmail("test@test.tld");
        dependent.setLastLogin(1354091L);
        dependent.setIsLocked(1);
        dependent.setIsActive(1);
        SampleColumns<?>[] columns = { SampleColumns.ID, SampleColumns.NAME, SampleColumns.EMAIL,
            SampleColumns.IS_LOCKED, SampleColumns.IS_ACTIVE, SampleColumns.LAST_LOGIN };

        // when
        boolean result = handler.insert(dependent, columns);

        // then
        assertThat(result, equalTo(true));
        assertThat(handler.retrieve(155, SampleColumns.NAME).getValue(), equalTo("Jason"));
        assertThat(handler.retrieve(155, SampleColumns.EMAIL).getValue(), nullValue());
        assertThat(handler.retrieve(155, SampleColumns.IP).getValue(), nullValue());
        assertThat(handler.retrieve(155, SampleColumns.IS_LOCKED).getValue(), equalTo(1));
        assertThat(handler.retrieve(155, SampleColumns.IS_ACTIVE).getValue(), equalTo(1));
        assertThat(handler.retrieve(155, SampleColumns.LAST_LOGIN).getValue(), equalTo(1354091L));
    }

    @Test
    public void shouldInsertUsingDefaultKeywordForNullValues() throws SQLException {
        assumeTrue(hasSupportForDefaultKeyword());

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
    public void shouldCountWithPredicates() throws SQLException {
        // given / when
        int emailCount = handler.count(eq(SampleColumns.EMAIL, "other@test.tld"));
        int ipLastLoginCount = handler.count(isNull(SampleColumns.IP).and(
            greaterThan(SampleColumns.LAST_LOGIN, 800000L)));
        int hasEmailAndIsActiveCount = handler.count(notEq(SampleColumns.EMAIL, "test@example.com")
            .and(greaterThanEquals(SampleColumns.IS_ACTIVE, 1)));
        int lockedAndActiveSameValue = handler.count(or(
            eq(SampleColumns.IS_ACTIVE, 0).and(eq(SampleColumns.IS_LOCKED, 0)),
            eq(SampleColumns.IS_ACTIVE, 1).and(eq(SampleColumns.IS_LOCKED, 1))));

        // then
        assertThat(emailCount, equalTo(3));
        assertThat(ipLastLoginCount, equalTo(2));
        assertThat(hasEmailAndIsActiveCount, equalTo(2));
        assertThat(lockedAndActiveSameValue, equalTo(4));
    }

    @Test
    public void shouldCountWithCaseInsensitivePredicate() throws SQLException {
        // given
        Predicate<SampleContext> predicate = eqIgnoreCase(SampleColumns.EMAIL, "TEST@example.com")
            .or(eqIgnoreCase(SampleColumns.NAME, "louis"));

        // when
        int result = handler.count(predicate);

        // then
        assertThat(result, equalTo(5));
    }

    @Test
    public void shouldRetrieveValuesAfterCaseInsensitiveCheck() throws SQLException {
        // given
        Predicate<SampleContext> predicate = or( // TODO: create or() with varargs
            notEqIgnoreCase(SampleColumns.NAME, "HANS").and(eqIgnoreCase(SampleColumns.EMAIL, "OTHER@test.tld")),
            eqIgnoreCase(SampleColumns.NAME, "keane").and(notEqIgnoreCase(SampleColumns.EMAIL, "TEST@example.COM")))
            .or(eqIgnoreCase(SampleColumns.NAME, "finn").and(notEqIgnoreCase(SampleColumns.EMAIL, "OTHER@test.tld")));

        // when
        List<DataSourceValues> result = handler.retrieve(predicate, SampleColumns.NAME, SampleColumns.EMAIL);

        // then
        List<String> names = result.stream().map(entry -> entry.get(SampleColumns.NAME)).collect(Collectors.toList());
        assertThat(names, containsInAnyOrder("Igor", "Louis", "Finn"));
    }

    private static void verifyThrowsException(Runnable runnable) {
        IllegalArgumentException ex = expectException(IllegalArgumentException.class, runnable::run);
        assertThat(ex.getMessage(), containsString("No value available for column"));
    }

    private static final class ColumnImpl<T> implements Column<T, SampleContext> {
        private final String name;
        private final ColumnType<T> type;

        private ColumnImpl(String name, ColumnType<T> type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String resolveName(SampleContext context) {
            return name;
        }

        @Override
        public ColumnType<T> getType() {
            return type;
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