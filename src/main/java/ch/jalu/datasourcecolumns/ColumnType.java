package ch.jalu.datasourcecolumns;

/**
 * Defines a column's value type.
 *
 * @param <T> the type of values stored in the column
 * @see StandardTypes
 * @see ch.jalu.datasourcecolumns.sqlimplementation.ResultSetValueRetriever
 */
public interface ColumnType<T> {

    /**
     * @return the class of the values stored in the column
     */
    Class<T> getClazz();

}
