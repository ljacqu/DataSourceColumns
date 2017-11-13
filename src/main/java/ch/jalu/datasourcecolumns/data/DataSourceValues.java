package ch.jalu.datasourcecolumns.data;

import ch.jalu.datasourcecolumns.Column;

/**
 * Holds multiple various of an entry in the data source.
 */
public interface DataSourceValues {

    /**
     * @return true if the row exists, false otherwise
     */
    boolean rowExists();

    /**
     * Gets the value of the associated column. Throws an exception
     * if the column was not requested.
     *
     * @param column the column to get the value for
     * @param <T> the value type
     * @return the value
     */
    <T> T get(Column<T, ?> column);
}
