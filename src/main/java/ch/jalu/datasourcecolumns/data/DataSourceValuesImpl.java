package ch.jalu.datasourcecolumns.data;

import ch.jalu.datasourcecolumns.Column;

import java.util.HashMap;
import java.util.Map;

public class DataSourceValuesImpl implements DataSourceValues {

    private static final DataSourceValuesImpl UNKNOWN_PLAYER = new DataSourceValuesImpl();

    private final Map<Column, Object> values = new HashMap<>();

    public static DataSourceValuesImpl unknownPlayer() {
        return UNKNOWN_PLAYER;
    }

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
    public boolean playerExists() {
        return this != UNKNOWN_PLAYER;
    }
}
