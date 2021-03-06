package ch.jalu.datasourcecolumns.sqlimplementation;

import ch.jalu.datasourcecolumns.Column;
import ch.jalu.datasourcecolumns.ColumnType;
import ch.jalu.datasourcecolumns.StandardTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Retrieves values from a SQL {@link ResultSet}.
 *
 * @param <C> the context type
 */
public class ResultSetValueRetriever<C> {

    private final C context;
    private final Map<ColumnType, ResultSetGetter> resultSetGetters = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param context the context
     */
    public ResultSetValueRetriever(C context) {
        this.context = context;
    }

    /**
     * Gets the value for the column from the given result set.
     *
     * @param rs the result set to fetch from
     * @param column the column to fetch the value for
     * @param <T> the value type
     * @return the fetched value
     */
    @SuppressWarnings("unchecked")
    public <T> T get(ResultSet rs, Column<T, C> column) throws SQLException {
        return ((ResultSetGetter<T>) resultSetGetters.computeIfAbsent(column.getType(), this::createResultSetGetter))
            .getValue(rs, column.resolveName(context));
    }

    /**
     * Returns a function based on the input type from which a value can be retrieved
     * from the give result set.
     *
     * @param type the type to create a getter for
     * @param <T> the type
     * @return the getter to use
     */
    @SuppressWarnings("unchecked")
    protected <T> ResultSetGetter<T> createResultSetGetter(ColumnType<T> type) {
        final ResultSetGetter resultSetGetter;
        if (type == StandardTypes.STRING) {
            resultSetGetter = ResultSet::getString;
        } else if (type == StandardTypes.LONG) {
            resultSetGetter = getTypeNullable(ResultSet::getLong, 0L);
        } else if (type == StandardTypes.INTEGER) {
            resultSetGetter = getTypeNullable(ResultSet::getInt, 0);
        } else if (type == StandardTypes.BOOLEAN) {
            resultSetGetter = getTypeNullable(ResultSet::getBoolean, false);
        } else if (type == StandardTypes.DOUBLE) {
            resultSetGetter = getTypeNullable(ResultSet::getDouble, 0.0);
        } else if (type == StandardTypes.FLOAT) {
            resultSetGetter = getTypeNullable(ResultSet::getFloat, 0.0f);
        } else {
            throw new IllegalArgumentException("Unhandled type '" + type + "'");
        }
        return resultSetGetter;
    }

    private static <T> ResultSetGetter<T> getTypeNullable(ResultSetGetter<T> getter, T standInValue) {
        return (rs, column) -> {
            final T value = getter.getValue(rs, column);
            return standInValue.equals(value) && rs.wasNull() ? null : value;
        };
    }

    @FunctionalInterface
    public interface ResultSetGetter<T> {

        /**
         * Retrieves the value from the provided ResultSet for the given column name.
         *
         * @param rs the result set to fetch a value from
         * @param column name of the column to fetch the value for
         * @return the retrieved value, may be null
         */
        T getValue(ResultSet rs, String column) throws SQLException;

    }
}
