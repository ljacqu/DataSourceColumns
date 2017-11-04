package ch.jalu.datasourcecolumns.sqlimplementation;

import ch.jalu.datasourcecolumns.SampleColumns;
import ch.jalu.datasourcecolumns.SampleContext;
import ch.jalu.datasourcecolumns.predicate.AlwaysTruePredicate;
import ch.jalu.datasourcecolumns.predicate.Predicate;
import ch.jalu.datasourcecolumns.sqlimplementation.PredicateSqlGenerator.WhereClauseResult;
import org.junit.Test;

import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.and;
import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.eq;
import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.greaterThan;
import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.greaterThanEquals;
import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.isNotNull;
import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.isNull;
import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.lessThan;
import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.lessThanEquals;
import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.notEq;
import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.or;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link PredicateSqlGenerator}.
 */
public class PredicateSqlGeneratorTest {

    private final SampleContext context = new SampleContext();
    private final PredicateSqlGenerator<SampleContext> predicateGenerator = new PredicateSqlGenerator<>(context);

    @Test
    public void shouldGenerateSqlForComparingPredicates() {
        // given
        Predicate<SampleContext> predicate1 = greaterThan(SampleColumns.IS_LOCKED, 2)
            .and(greaterThanEquals(SampleColumns.LAST_LOGIN, 1000L));
        Predicate<SampleContext> predicate2 = lessThan(SampleColumns.IS_ACTIVE, -5)
            .or(lessThanEquals(SampleColumns.IS_LOCKED, 3));
        Predicate<SampleContext> predicate3 = eq(SampleColumns.NAME, "Jamie")
            .and(notEq(SampleColumns.LAST_LOGIN, 13L));

        // when
        WhereClauseResult result1 = predicateGenerator.generateWhereClause(predicate1);
        WhereClauseResult result2 = predicateGenerator.generateWhereClause(predicate2);
        WhereClauseResult result3 = predicateGenerator.generateWhereClause(predicate3);

        // then
        assertThat(result1.getGeneratedSql(), equalTo("(is_locked > ?) AND (last_login >= ?)"));
        assertThat(result1.getBindings(), contains(2, 1000L));
        assertThat(result2.getGeneratedSql(), equalTo("(is_active < ?) OR (is_locked <= ?)"));
        assertThat(result2.getBindings(), contains(-5, 3));
        assertThat(result3.getGeneratedSql(), equalTo("(username = ?) AND (last_login <> ?)"));
        assertThat(result3.getBindings(), contains("Jamie", 13L));
    }

    @Test
    public void shouldGenerateSqlForComparingPredicatesWithEmptyColumns() {
        // given
        context.setEmptyOptions(true, true, false);

        Predicate<SampleContext> predicate1 = greaterThan(SampleColumns.IS_LOCKED, 2)
            .and(greaterThanEquals(SampleColumns.LAST_LOGIN, 1000L));
        Predicate<SampleContext> predicate2 = lessThan(SampleColumns.IS_ACTIVE, -5)
            .or(lessThanEquals(SampleColumns.IS_LOCKED, 3));
        Predicate<SampleContext> predicate3 = eq(SampleColumns.EMAIL, "test@example.org")
            .and(notEq(SampleColumns.LAST_LOGIN, 13L));

        // when
        WhereClauseResult result1 = predicateGenerator.generateWhereClause(predicate1);
        WhereClauseResult result2 = predicateGenerator.generateWhereClause(predicate2);
        WhereClauseResult result3 = predicateGenerator.generateWhereClause(predicate3);

        // then
        assertThat(result1.getGeneratedSql(), equalTo("(1 = 1) AND (last_login >= ?)"));
        assertThat(result1.getBindings(), contains(1000L));
        assertThat(result2.getGeneratedSql(), equalTo("(is_active < ?) OR (1 = 1)"));
        assertThat(result2.getBindings(), contains(-5));
        assertThat(result3.getGeneratedSql(), equalTo("(1 = 1) AND (last_login <> ?)"));
        assertThat(result3.getBindings(), contains(13L));
    }

    @Test
    public void shouldGenerateSqlForNullPredicates() {
        // given
        context.setEmptyOptions(true, false, true);

        Predicate<SampleContext> predicate1 = or(isNull(SampleColumns.ID), isNotNull(SampleColumns.NAME));
        Predicate<SampleContext> predicate2 = and(isNull(SampleColumns.EMAIL).or(isNotNull(SampleColumns.IS_LOCKED)),
            isNull(SampleColumns.IP));
        Predicate<SampleContext> predicate3 = isNotNull(SampleColumns.IP)
            .or(isNull(SampleColumns.LAST_LOGIN))
            .and(isNull(SampleColumns.IS_LOCKED));

        // when
        WhereClauseResult result1 = predicateGenerator.generateWhereClause(predicate1);
        WhereClauseResult result2 = predicateGenerator.generateWhereClause(predicate2);
        WhereClauseResult result3 = predicateGenerator.generateWhereClause(predicate3);

        // then
        assertThat(result1.getGeneratedSql(), equalTo("(id IS NULL) OR (username IS NOT NULL)"));
        assertThat(result1.getBindings(), empty());
        assertThat(result2.getGeneratedSql(), equalTo("((1 = 1) OR (is_locked IS NOT NULL)) AND (ip IS NULL)"));
        assertThat(result2.getBindings(), empty());
        assertThat(result3.getGeneratedSql(), equalTo("((ip IS NOT NULL) OR (1 = 1)) AND (is_locked IS NULL)"));
        assertThat(result3.getBindings(), empty());
    }

    @Test
    public void shouldGenerateSqlForAlwaysTruePredicate() {
        // given
        Predicate<SampleContext> predicate = new AlwaysTruePredicate<>();

        // when
        WhereClauseResult result = predicateGenerator.generateWhereClause(predicate);

        // then
        assertThat(result.getGeneratedSql(), equalTo("1 = 1"));
        assertThat(result.getBindings(), empty());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowForUnknownPredicate() {
        // given
        Predicate<SampleContext> unknownPredicate = mock(Predicate.class);

        // when
        predicateGenerator.generateWhereClause(unknownPredicate);

        // then - expect exception to be thrown
    }
}
