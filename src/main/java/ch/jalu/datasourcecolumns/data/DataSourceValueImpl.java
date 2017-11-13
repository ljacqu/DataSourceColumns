package ch.jalu.datasourcecolumns.data;

/**
 * Implementation of {@link DataSourceValue}.
 */
public final class DataSourceValueImpl<T> implements DataSourceValue<T> {

    /** Instance used when a row does not exist. */
    private static final DataSourceValueImpl UNKNOWN_ROW = new DataSourceValueImpl<>(null);
    private final T value;

    private DataSourceValueImpl(T value) {
        this.value = value;
    }

    /**
     * Returns a {@link DataSourceValueImpl} for the given value.
     *
     * @param value the value to wrap
     * @param <T> the value's type
     * @return DataSourceResult object for the given value
     */
    public static <T> DataSourceValueImpl<T> of(T value) {
        return new DataSourceValueImpl<>(value);
    }

    /**
     * Returns a {@link DataSourceValueImpl} specifying that the row does not exist.
     *
     * @param <T> the value type
     * @return data source result for non-existent row
     */
    public static <T> DataSourceValueImpl<T> unknownRow() {
        return UNKNOWN_ROW;
    }

    @Override
    public boolean rowExists() {
        return this != UNKNOWN_ROW;
    }

    @Override
    public T getValue() {
        return value;
    }
}
