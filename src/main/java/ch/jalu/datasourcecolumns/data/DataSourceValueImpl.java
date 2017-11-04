package ch.jalu.datasourcecolumns.data;

/**
 * Implementation of {@link DataSourceValue}.
 */
public final class DataSourceValueImpl<T> implements DataSourceValue<T> {

    /** Instance used when a player does not exist. */
    private static final DataSourceValueImpl UNKNOWN_PLAYER = new DataSourceValueImpl<>(null);
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
     * Returns a {@link DataSourceValueImpl} specifying that the player does not exist.
     *
     * @param <T> the value type
     * @return data source result for unknown player
     */
    public static <T> DataSourceValueImpl<T> unknownPlayer() {
        return UNKNOWN_PLAYER;
    }

    @Override
    public boolean playerExists() {
        return this != UNKNOWN_PLAYER;
    }

    @Override
    public T getValue() {
        return value;
    }
}
