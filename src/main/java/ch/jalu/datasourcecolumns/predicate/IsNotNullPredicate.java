package ch.jalu.datasourcecolumns.predicate;

import ch.jalu.datasourcecolumns.Column;

/**
 * Predicate matching if a column is not null.
 *
 * @param <C> the context type
 */
public class IsNotNullPredicate<C> extends AbstractPredicate<C> {

    private final Column<?, C> column;

    public IsNotNullPredicate(Column<?, C> column) {
        this.column = column;
    }

    public Column<?, C> getColumn() {
        return column;
    }
}
