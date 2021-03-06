package ch.jalu.datasourcecolumns.predicate;

import ch.jalu.datasourcecolumns.Column;

import static ch.jalu.datasourcecolumns.predicate.ComparingPredicate.Type.EQUALS;
import static ch.jalu.datasourcecolumns.predicate.ComparingPredicate.Type.GREATER;
import static ch.jalu.datasourcecolumns.predicate.ComparingPredicate.Type.GREATER_EQUALS;
import static ch.jalu.datasourcecolumns.predicate.ComparingPredicate.Type.LESS;
import static ch.jalu.datasourcecolumns.predicate.ComparingPredicate.Type.LESS_EQUALS;
import static ch.jalu.datasourcecolumns.predicate.ComparingPredicate.Type.NOT_EQUALS;

/**
 * Class with static methods to generate predicates. Meant to be imported statically.
 * <p>
 * If you add more predicates note that you can extend this class.
 */
public class StandardPredicates {

    protected StandardPredicates() {
    }

    public static <T, C> ComparingPredicate<T, C> eq(Column<T, C> column, T requiredValue) {
        return new ComparingPredicate<>(column, requiredValue, EQUALS);
    }

    public static <T, C> ComparingPredicate<T, C> notEq(Column<T, C> column, T requiredValue) {
        return new ComparingPredicate<>(column, requiredValue, NOT_EQUALS);
    }

    public static <C> EqualsIgnoreCasePredicate<C> eqIgnoreCase(Column<String, C> column, String requiredValue) {
        return new EqualsIgnoreCasePredicate<>(column, requiredValue, false);
    }

    public static <C> EqualsIgnoreCasePredicate<C> notEqIgnoreCase(Column<String, C> column, String requiredValue) {
        return new EqualsIgnoreCasePredicate<>(column, requiredValue, true);
    }

    public static <T, C> ComparingPredicate<T, C> greaterThan(Column<T, C> column, T requiredValue) {
        return new ComparingPredicate<>(column, requiredValue, GREATER);
    }

    public static <T, C> ComparingPredicate<T, C> greaterThanEquals(Column<T, C> column, T requiredValue) {
        return new ComparingPredicate<>(column, requiredValue, GREATER_EQUALS);
    }

    public static <T, C> ComparingPredicate<T, C> lessThan(Column<T, C> column, T requiredValue) {
        return new ComparingPredicate<>(column, requiredValue, LESS);
    }

    public static <T, C> ComparingPredicate<T, C> lessThanEquals(Column<T, C> column, T requiredValue) {
        return new ComparingPredicate<>(column, requiredValue, LESS_EQUALS);
    }

    public static <C> IsNullPredicate<C> isNull(Column<?, C> column) {
        return new IsNullPredicate<>(column);
    }

    public static <C> IsNotNullPredicate<C> isNotNull(Column<?, C> column) {
        return new IsNotNullPredicate<>(column);
    }

    public static <C> AndPredicate<C> and(Predicate<C> left, Predicate<C> right) {
        return new AndPredicate<>(left, right);
    }

    public static <C> OrPredicate<C> or(Predicate<C> left, Predicate<C> right) {
        return new OrPredicate<>(left, right);
    }
}
