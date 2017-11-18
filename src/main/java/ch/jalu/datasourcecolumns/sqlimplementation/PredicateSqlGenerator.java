package ch.jalu.datasourcecolumns.sqlimplementation;

import ch.jalu.datasourcecolumns.Column;
import ch.jalu.datasourcecolumns.predicate.AlwaysTruePredicate;
import ch.jalu.datasourcecolumns.predicate.AndPredicate;
import ch.jalu.datasourcecolumns.predicate.ComparingPredicate;
import ch.jalu.datasourcecolumns.predicate.EqualsIgnoreCasePredicate;
import ch.jalu.datasourcecolumns.predicate.IsNotNullPredicate;
import ch.jalu.datasourcecolumns.predicate.IsNullPredicate;
import ch.jalu.datasourcecolumns.predicate.OrPredicate;
import ch.jalu.datasourcecolumns.predicate.Predicate;

import java.util.LinkedList;
import java.util.List;

/**
 * Converts {@link Predicate} instances to SQL.
 *
 * @param <C> the context type
 */
public class PredicateSqlGenerator<C> {

    private final C context;
    private final boolean useNoCaseCollationForCaseInsensitiveEquals;

    /**
     * Constructor.
     *
     * @param context the context
     */
    public PredicateSqlGenerator(C context) {
        this(context, false);
    }

    /**
     * Constructor.
     *
     * @param context the context
     * @param useNoCaseCollationForCaseInsensitiveEquals true to generate SQL with {@code COLLATE NOCASE} for
     *        {@link EqualsIgnoreCasePredicate}. Not supported and/or needed by all database engines;
     *        see class JavaDoc on {@link EqualsIgnoreCasePredicate}.
     */
    public PredicateSqlGenerator(C context, boolean useNoCaseCollationForCaseInsensitiveEquals) {
        this.context = context;
        this.useNoCaseCollationForCaseInsensitiveEquals = useNoCaseCollationForCaseInsensitiveEquals;
    }

    /**
     * Generates SQL to use as {@code WHERE} clause in a query.
     *
     * @param predicate the predicate to convert to SQL
     * @return the generated SQL code
     */
    public GeneratedSqlWithBindings generateWhereClause(Predicate<C> predicate) {
        StringBuilder sqlResult = new StringBuilder();
        List<Object> bindings = new LinkedList<>();
        generateWhereClause(predicate, sqlResult, bindings);
        return new GeneratedSqlWithBindings(sqlResult.toString(), bindings);
    }

    protected void generateWhereClause(Predicate<C> predicate, StringBuilder sqlResult, List<Object> objects) {
        final Class<?> clazz = predicate.getClass();
        if (clazz == ComparingPredicate.class) {
            ComparingPredicate<?, C> eq = (ComparingPredicate<?, C>) predicate;
            processComparingClause(eq, sqlResult, objects);
        } else if (clazz == EqualsIgnoreCasePredicate.class) {
            EqualsIgnoreCasePredicate<C> equalsIgnore = (EqualsIgnoreCasePredicate<C>) predicate;
            processEqualsIgnoreCasePredicate(equalsIgnore, sqlResult, objects);
        } else if (clazz == OrPredicate.class) {
            OrPredicate<C> or = (OrPredicate<C>) predicate;
            processCombiningClause(or.getLeft(), or.getRight(), "OR", sqlResult, objects);
        } else if (clazz == AndPredicate.class) {
            AndPredicate<C> and = (AndPredicate<C>) predicate;
            processCombiningClause(and.getLeft(), and.getRight(), "AND", sqlResult, objects);
        } else if (clazz == IsNullPredicate.class) {
            IsNullPredicate<C> isNull = (IsNullPredicate<C>) predicate;
            processIsNullAndNotNullPredicate(false, isNull.getColumn(), sqlResult);
        } else if (clazz == IsNotNullPredicate.class) {
            IsNotNullPredicate<C> isNotNull = (IsNotNullPredicate<C>) predicate;
            processIsNullAndNotNullPredicate(true, isNotNull.getColumn(), sqlResult);
        } else if (clazz == AlwaysTruePredicate.class) {
            addAlwaysTruePredicate(sqlResult);
        } else {
            throw new IllegalStateException("Unhandled predicate '" + predicate + "'");
        }
    }

    protected void addAlwaysTruePredicate(StringBuilder sqlResult) {
        sqlResult.append("1 = 1");
    }

    protected void processComparingClause(ComparingPredicate<?, C> predicate, StringBuilder sqlResult,
                                          List<Object> objects) {
        if (predicate.getColumn().isColumnUsed(context)) {
            sqlResult.append(predicate.getColumn().resolveName(context))
                .append(convertComparingTypeToSqlOperator(predicate.getType()))
                .append("?");
            objects.add(predicate.getValue());
        } else {
            addAlwaysTruePredicate(sqlResult);
        }
    }

    /**
     * Adds the SQL code for the given predicate. Please note that class Javadoc on {@link EqualsIgnoreCasePredicate}
     * for an important caveat!
     *
     * @param predicate the predicate to generate SQL for
     * @param sqlResult the string builder saving the SQL for all predicates
     * @param objects list objects to bind to the generated SQL code
     */
    protected void processEqualsIgnoreCasePredicate(EqualsIgnoreCasePredicate<C> predicate, StringBuilder sqlResult,
                                                    List<Object> objects) {
        if (predicate.getColumn().isColumnUsed(context)) {
            final String operator = predicate.isNegated() ? " <> ?" : " = ?";
            sqlResult.append(predicate.getColumn().resolveName(context))
                .append(operator);
            if (useNoCaseCollationForCaseInsensitiveEquals) {
                sqlResult.append(" COLLATE NOCASE");
            }
            objects.add(predicate.getValue());
        } else {
            addAlwaysTruePredicate(sqlResult);
        }
    }

    protected String convertComparingTypeToSqlOperator(ComparingPredicate.Type type) {
        switch (type) {
            case LESS:           return " < ";
            case LESS_EQUALS:    return " <= ";
            case EQUALS:         return " = ";
            case NOT_EQUALS:     return " <> ";
            case GREATER:        return " > ";
            case GREATER_EQUALS: return " >= ";
            default:
                throw new IllegalStateException("Unknown comparing predicate type '" + type + "'");
        }
    }

    protected void processIsNullAndNotNullPredicate(boolean isNegated, Column<?, C> column, StringBuilder sqlResult) {
        if (column.isColumnUsed(context)) {
            final String condition = isNegated ? " IS NOT NULL" : " IS NULL";
            sqlResult.append(column.resolveName(context)).append(condition);
        } else {
            addAlwaysTruePredicate(sqlResult);
        }
    }

    protected void processCombiningClause(Predicate<C> left, Predicate<C> right, String operator,
                                          StringBuilder sqlResult, List<Object> objects) {
        sqlResult.append("(");
        generateWhereClause(left, sqlResult, objects);
        sqlResult.append(") ").append(operator).append(" (");
        generateWhereClause(right, sqlResult, objects);
        sqlResult.append(")");
    }
}
