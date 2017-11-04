package ch.jalu.datasourcecolumns.data;

import ch.jalu.datasourcecolumns.Column;


public interface DataSourceValues {

    /**
     * @return whether the player of the associated value exists
     */
    boolean playerExists(); // TODO: rename

    <T> T get(Column<T, ?> column);
}
