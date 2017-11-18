package ch.jalu.datasourcecolumns.predicate;

import ch.jalu.datasourcecolumns.Column;

/**
 * Predicate for a case-insensitive equals check.
 * <p>
 * <b>WARNING:</b> This functionality depends on your database engine. When using this, check that it behaves correctly
 * and verify that the generated SQL isn't suboptimal in regards to performance. See also the constructors in
 * {@link ch.jalu.datasourcecolumns.sqlimplementation.PredicateSqlGenerator}.
 * <p>
 * In <b>MySQL</b>, columns are case-insensitive by default, so the value in
 * {@link ch.jalu.datasourcecolumns.sqlimplementation.PredicateSqlGenerator#useNoCaseCollationForCaseInsensitiveEquals}
 * should be set to {@code false} for optimal performance.
 * <p>
 * In <b>SQLite</b>, columns are by default case-sensitive. Here, the above value should be set to {@code true} so that
 * the predicate SQL generator performs case-insensitive checks with {@code COLLATE NOCASE}. Note that by default this
 * will only work with ASCII characters, cf. SQLite's FAQ,
 * <a href="https://sqlite.org/faq.html#q18">Case-insensitive matching of Unicode characters does not work</a>.
 * <p>
 * <b>H2</b> is by default case-sensitive but does not support the {@code COLLATE NOCASE} syntax. When connecting, you
 * can specify {@code ignorecase=true} in the connection URL.
 *
 * @param <C> the context
 */
public class EqualsIgnoreCasePredicate<C> extends AbstractPredicate<C> {

    private final Column<String, C> column;
    private final String value;
    private final boolean isNegated;

    public EqualsIgnoreCasePredicate(Column<String, C> column, String value, boolean isNegated) {
        this.column = column;
        this.value = value;
        this.isNegated = isNegated;
    }

    public Column<String, C> getColumn() {
        return column;
    }

    public String getValue() {
        return value;
    }

    public boolean isNegated() {
        return isNegated;
    }
}
