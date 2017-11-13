package ch.jalu.datasourcecolumns.data;

import ch.jalu.datasourcecolumns.Column;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link DataSourceValues}.
 */
public class DataSourceValuesImpl implements DataSourceValues {

    private static final DataSourceValuesImpl UNKNOWN_ROW = new DataSourceValuesImpl();

    private final Map<Column, Object> values = new HashMap<>();

    /**
     * @return {@link DataSourceValues} instance to represent a non-existent row
     */
    public static DataSourceValuesImpl unknownRow() {
        return UNKNOWN_ROW;
    }

    /**
     * Adds the provided value for the given column.
     *
     * @param column the column to save the value for
     * @param value the value to save
     * @param <T> the value type
     */
    public <T> void put(Column<T, ?> column, Object value) {
        final Class<T> typeClass = column.getType().getClazz();
        if (value == null || typeClass.isInstance(value)) {
            values.put(column, value);
        } else {
            throw new IllegalArgumentException(
                "Value '" + value + "' does not have the correct type for column '" + column + "'");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Column<T, ?> column) {
        final T value = (T) values.get(column);
        if (value == null && !values.containsKey(column)) {
            throw new IllegalArgumentException("No value available for column '" + column + "'");
        }
        return value;
    }

    @Override
    public boolean rowExists() {
        return this != UNKNOWN_ROW;
    }
}
