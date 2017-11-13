package ch.jalu.datasourcecolumns.data;

/**
 * Wraps a value and allows to specify whether a row was found.
 */
public interface DataSourceValue<T> {

    /**
     * @return true if the row exists, false otherwise
     */
    boolean rowExists();

    /**
     * Returns the value. It is {@code null} if the value is null
     * or the row does not exist.
     *
     * @return the value, or null
     */
    T getValue();

}
