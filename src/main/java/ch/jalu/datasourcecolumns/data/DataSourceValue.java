package ch.jalu.datasourcecolumns.data;

/**
 * Wraps a value and allows to specify whether a value is missing or the player is not registered.
 */
public interface DataSourceValue<T> {

    /**
     * @return whether the player of the associated value exists
     */
    boolean playerExists(); // TODO: rename

    /**
     * Returns the value. It is {@code null} if the player is unknown. It is also {@code null}
     * if the player exists but does not have the value defined.
     *
     * @return the value, or null
     */
    T getValue();

}
