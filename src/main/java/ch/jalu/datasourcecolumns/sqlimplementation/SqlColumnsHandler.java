package ch.jalu.datasourcecolumns.sqlimplementation;

import ch.jalu.datasourcecolumns.Column;
import ch.jalu.datasourcecolumns.ColumnsHandler;
import ch.jalu.datasourcecolumns.DependentColumn;
import ch.jalu.datasourcecolumns.data.DataSourceValue;
import ch.jalu.datasourcecolumns.data.DataSourceValueImpl;
import ch.jalu.datasourcecolumns.data.DataSourceValues;
import ch.jalu.datasourcecolumns.data.DataSourceValuesImpl;
import ch.jalu.datasourcecolumns.data.UpdateValues;
import ch.jalu.datasourcecolumns.predicate.Predicate;
import ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator.PreparedStatementGeneratorFactory;
import ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator.PreparedStatementResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator.PreparedStatementGeneratorFactory.fromConnection;

/**
 * Implementation of {@link ColumnsHandler} for a SQL data source.
 *
 * @param <C> the context type
 * @param <I> the identifier type
 */
public class SqlColumnsHandler<C, I> implements ColumnsHandler<C, I> {

    private final PreparedStatementGeneratorFactory statementGeneratorFactory;
    private final String tableName;
    private final String idColumn;
    private final ResultSetValueRetriever<C> resultSetValueRetriever;
    private final PredicateSqlGenerator<C> predicateSqlGenerator;
    private final C context;

    /**
     * Constructor.
     *
     * @param connection connection to the database
     * @param context the context object (for name resolution)
     * @param tableName name of the SQL table
     * @param idColumn the name of the identifier column
     */
    public SqlColumnsHandler(Connection connection, C context, String tableName, String idColumn) {
        this(fromConnection(connection), context, tableName, idColumn,
            new ResultSetValueRetriever<>(context), new PredicateSqlGenerator<>(context));
    }

    /**
     * Constructor.
     *
     * @param statementGeneratorFactory factory for creating PreparedStatement generators
     * @param context the context object (for name resolution)
     * @param tableName name of the SQL table
     * @param idColumn the name of the identifier column
     * @param resultSetValueRetriever instance to use to retrieve values from a result set
     * @param predicateSqlGenerator SQL generator for predicates
     */
    public SqlColumnsHandler(PreparedStatementGeneratorFactory statementGeneratorFactory, C context, String tableName,
                             String idColumn, ResultSetValueRetriever<C> resultSetValueRetriever,
                             PredicateSqlGenerator<C> predicateSqlGenerator) {
        this.context = context;
        this.resultSetValueRetriever = resultSetValueRetriever;
        this.predicateSqlGenerator = predicateSqlGenerator;
        this.tableName = tableName;
        this.statementGeneratorFactory = statementGeneratorFactory;
        this.idColumn = idColumn;
    }

    @Override
    public <T> DataSourceValue<T> retrieve(I identifier, Column<T, C> column) throws SQLException {
        final boolean isColumnUsed = column.isColumnUsed(context);
        final String columnName = isColumnUsed ? column.resolveName(context) : "1";
        final String sql = "SELECT " + columnName + " FROM " + tableName + " WHERE " + idColumn + " = ?;";

        try (PreparedStatementResult result = statementGeneratorFactory.create(sql)) {
            final PreparedStatement pst = result.getPreparedStatement();
            pst.setObject(1, identifier);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return isColumnUsed ? DataSourceValueImpl.of(resultSetValueRetriever.get(rs, column))
                        : DataSourceValueImpl.of(null);
                }
                return DataSourceValueImpl.unknownRow();
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public DataSourceValues retrieve(I identifier, Column<?, C>... columns) throws SQLException {
        final Set<Column<?, C>> nonEmptyColumns = removeSkippedColumns(columns);
        final String sql = "SELECT " + (nonEmptyColumns.isEmpty() ? "1" : commaSeparatedList(nonEmptyColumns))
            + " FROM " + tableName + " WHERE " + idColumn + " = ?;";

        try (PreparedStatementResult result = statementGeneratorFactory.create(sql)) {
            final PreparedStatement pst = result.getPreparedStatement();
            pst.setObject(1, identifier);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next()
                    ? generateDataSourceValuesObject(rs, nonEmptyColumns, columns)
                    : DataSourceValuesImpl.unknownRow();
            }
        }
    }

    @Override
    public <T> List<T> retrieve(Predicate<C> predicate, Column<T, C> column) throws SQLException {
        final boolean isColumnUsed = column.isColumnUsed(context);
        if (!isColumnUsed) {
            final int matchingRows = count(predicate);
            return Collections.nCopies(matchingRows, null);
        }

        final GeneratedSqlWithBindings sqlPredicate = predicateSqlGenerator.generateWhereClause(predicate);
        final String sql = "SELECT " + column.resolveName(context)
            + " FROM " + tableName + " WHERE " + sqlPredicate.getGeneratedSql();

        List<T> results = new ArrayList<>();
        try (PreparedStatementResult result = statementGeneratorFactory.create(sql)) {
            final PreparedStatement pst = result.getPreparedStatement();
            bindValues(pst, 1, sqlPredicate.getBindings());
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    results.add(resultSetValueRetriever.get(rs, column));
                }
            }
        }
        return results;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DataSourceValues> retrieve(Predicate<C> predicate, Column<?, C>... columns) throws SQLException {
        final Set<Column<?, C>> nonEmptyColumns = removeSkippedColumns(columns);
        final GeneratedSqlWithBindings sqlPredicate = predicateSqlGenerator.generateWhereClause(predicate);
        final String sql = "SELECT " + (nonEmptyColumns.isEmpty() ? "1" : commaSeparatedList(nonEmptyColumns))
            + " FROM " + tableName + " WHERE " + sqlPredicate.getGeneratedSql();

        List<DataSourceValues> matchingEntries = new ArrayList<>();
        try (PreparedStatementResult result = statementGeneratorFactory.create(sql)) {
            final PreparedStatement pst = result.getPreparedStatement();
            bindValues(pst, 1, sqlPredicate.getBindings());
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    DataSourceValues values = generateDataSourceValuesObject(rs, nonEmptyColumns, columns);
                    matchingEntries.add(values);
                }
            }
        }
        return matchingEntries;
    }

    @Override
    public <T> boolean update(I identifier, Column<T, C> column, T value) throws SQLException {
        if (!column.isColumnUsed(context)) {
            return true;
        } else if (value == null && column.useDefaultForNullValue(context)) {
            String sql = "UPDATE " + tableName + " SET " + column.resolveName(context)
                + " = DEFAULT WHERE " + idColumn + " = ?;";
            try (PreparedStatementResult result = statementGeneratorFactory.create(sql)) {
                final PreparedStatement pst = result.getPreparedStatement();
                pst.setObject(1, identifier);
                return performUpdateAction(pst);
            }
        }
        String sql = "UPDATE " + tableName + " SET " + column.resolveName(context)
            + " = ? WHERE " + idColumn + " = ?;";
        try (PreparedStatementResult result = statementGeneratorFactory.create(sql)) {
            final PreparedStatement pst = result.getPreparedStatement();
            pst.setObject(1, value);
            pst.setObject(2, identifier);
            return performUpdateAction(pst);
        }
    }

    @Override
    public boolean update(I identifier, UpdateValues<C> updateValues) throws SQLException {
        return performUpdate(
            identifier,
            updateValues.getColumns(),
            updateValues::get);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D> boolean update(I identifier, D dependent, DependentColumn<?, C, D>... columns) throws SQLException {
        return performUpdate(
            identifier,
            Arrays.asList(columns),
            column -> column.getValueFromDependent(dependent));
    }

    @Override
    public <T> int update(Predicate<C> predicate, Column<T, C> column, T value) throws SQLException {
        return update(predicate, UpdateValues.with(column, value).build());
    }

    @Override
    public int update(Predicate<C> predicate, UpdateValues<C> updateValues) throws SQLException {
        return performPredicateUpdate(predicate, updateValues);
    }

    @Override
    public boolean insert(UpdateValues<C> updateValues) throws SQLException {
        return performInsert(updateValues.getColumns(), updateValues::get);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D> boolean insert(D dependent, DependentColumn<?, C, D>... columns) throws SQLException {
        return performInsert(Arrays.asList(columns), column -> column.getValueFromDependent(dependent));
    }

    @Override
    public int count(Predicate<C> predicate) throws SQLException {
        GeneratedSqlWithBindings whereResult = predicateSqlGenerator.generateWhereClause(predicate);
        String sql = "SELECT COUNT(1) FROM " + tableName + " WHERE " + whereResult.getGeneratedSql();
        try (PreparedStatementResult result = statementGeneratorFactory.create(sql)) {
            final PreparedStatement pst = result.getPreparedStatement();
            bindValues(pst, 1, whereResult.getBindings());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new IllegalStateException("Could not fetch count for SQL '" + sql + "'");
            }
        }
    }

    private <E extends Column<?, C>> boolean performUpdate(I identifier, Collection<E> columns,
                                                           Function<E, Object> valueGetter) throws SQLException{
        final Set<E> nonEmptyColumns = removeSkippedColumns(columns);
        if (nonEmptyColumns.isEmpty()) {
            return false;
        }

        final GeneratedSqlWithBindings columnSetList = createColumnsListForUpdate(nonEmptyColumns, valueGetter);
        final String sql = "UPDATE " + tableName + " SET "
            + columnSetList.getGeneratedSql() + " WHERE " + idColumn + " = ?;";
        try (PreparedStatementResult result = statementGeneratorFactory.create(sql)) {
            final PreparedStatement pst = result.getPreparedStatement();
            int index = bindValues(pst, 1, columnSetList.getBindings());
            pst.setObject(index, identifier);
            return performUpdateAction(pst);
        }
    }

    private int performPredicateUpdate(Predicate<C> predicate, UpdateValues<C> updateValues) throws SQLException {
        final Set<Column<?, C>> nonEmptyColumns = removeSkippedColumns(updateValues.getColumns());
        if (nonEmptyColumns.isEmpty()) {
            return 0;
        }

        final GeneratedSqlWithBindings columnSetList = createColumnsListForUpdate(nonEmptyColumns, updateValues::get);
        final GeneratedSqlWithBindings whereClause = predicateSqlGenerator.generateWhereClause(predicate);
        final String sql = "UPDATE " + tableName + " SET " + columnSetList.getGeneratedSql()
            + " WHERE " + whereClause.getGeneratedSql();
        try (PreparedStatementResult result = statementGeneratorFactory.create(sql)) {
            final PreparedStatement pst = result.getPreparedStatement();
            int index = bindValues(pst, 1, columnSetList.getBindings());
            bindValues(pst, index, whereClause.getBindings());
            return pst.executeUpdate();
        }
    }

    private <E extends Column<?, C>> GeneratedSqlWithBindings createColumnsListForUpdate(
        Collection<E> columns, Function<E, Object> valueGetter) {

        final List<Object> bindings = new LinkedList<>();
        final String sql = columns.stream()
            .map(column -> {
                final Object value = valueGetter.apply(column);
                if (value == null && column.useDefaultForNullValue(context)) {
                    return column.resolveName(context) + " = DEFAULT";
                } else {
                    bindings.add(value);
                    return column.resolveName(context) + " = ?";
                }
            })
            .collect(Collectors.joining(", "));
        return new GeneratedSqlWithBindings(sql, bindings);
    }

    private <E extends Column<?, C>> boolean performInsert(Collection<E> columns,
                                                           Function<E, Object> valueGetter) throws SQLException {
        final Set<E> nonEmptyColumns = removeSkippedColumns(columns);
        if (nonEmptyColumns.isEmpty()) {
            throw new IllegalStateException("Cannot perform insert when all columns are empty: " + columns);
        }

        final GeneratedSqlWithBindings placeholders = createValuePlaceholdersForInsert(nonEmptyColumns, valueGetter);
        final String sql = "INSERT INTO " + tableName + " (" + commaSeparatedList(nonEmptyColumns) + ") "
            + "VALUES(" + placeholders.getGeneratedSql() + ");";
        try (PreparedStatementResult result = statementGeneratorFactory.create(sql)) {
            final PreparedStatement pst = result.getPreparedStatement();
            bindValues(pst, 1, placeholders.getBindings());
            return performUpdateAction(pst);
        }
    }

    private <E extends Column<?, C>> GeneratedSqlWithBindings createValuePlaceholdersForInsert(
        Collection<E> columns, Function<E, Object> valueGetter) {

        final List<Object> bindings = new LinkedList<>();
        final String sql = columns.stream()
            .map(column -> {
                final Object value = valueGetter.apply(column);
                if (value == null && column.useDefaultForNullValue(context)) {
                    return "DEFAULT";
                } else {
                    bindings.add(value);
                    return "?";
                }
            })
            .collect(Collectors.joining(", "));
        return new GeneratedSqlWithBindings(sql, bindings);
    }

    /**
     * Creates a {@link DataSourceValues} with all values of the given {@code columns}.
     *
     * @param rs the result set to fetch values from
     * @param nonEmptyColumns non-empty columns (columns which should not be ignored)
     * @param columns all columns that were requested for retrieval
     * @return generated DataSourceValues instance containing the columns' values
     */
    private DataSourceValues generateDataSourceValuesObject(ResultSet rs, Set<Column<?, C>> nonEmptyColumns,
                                                            Column<?, C>[] columns) throws SQLException {
        DataSourceValuesImpl values = new DataSourceValuesImpl();
        for (Column<?, C> column : columns) {
            if (nonEmptyColumns.contains(column)) {
                values.put(column, resultSetValueRetriever.get(rs, column));
            } else {
                values.put(column, null);
            }
        }
        return values;
    }

    /**
     * Wraps {@link PreparedStatement#executeUpdate()} for UPDATE and INSERT statements and returns a boolean
     * based on its return value.
     *
     * @param pst the prepared statement
     * @return true if one row was updated, false otherwise
     * @throws IllegalStateException if more than one row was updated (should never happen)
     * @throws SQLException on SQL errors
     */
    protected boolean performUpdateAction(PreparedStatement pst) throws SQLException {
        int count = pst.executeUpdate();
        if (count == 1) {
            return true;
        } else if (count == 0) {
            return false;
        } else {
            throw new IllegalStateException("Found " + count + " rows updated/inserted by statement, expected only 1");
        }
    }

    /*
     * Creates a comma-separated list with the given columns' names.
     */
    private String commaSeparatedList(Collection<? extends Column<?, C>> columns) {
        return columns.stream()
            .map(column -> column.resolveName(context))
            .collect(Collectors.joining(", "));
    }

    /**
     * Binds the given values to the PreparedStatement, starting from the given index (to allow
     * values to be bound before this method is called). The index at which binding can continue
     * is returned (to allow more values to be bound after calling this method).
     *
     * @param pst the prepared statement
     * @param startIndex the index at which value binding should begin
     * @param bindings the values to bind
     * @return the index at which binding should continue (if applicable)
     */
    private int bindValues(PreparedStatement pst, int startIndex,
                           Collection<Object> bindings) throws SQLException {
        int index = startIndex;
        for (Object binding : bindings) {
            pst.setObject(index, binding);
            ++index;
        }
        return index;
    }

    /**
     * Returns a Set of columns without any that should be skipped
     * (as determined by {@link Column#isColumnUsed}.
     *
     * @param cols the columns to filter
     * @param <E> the column extension type
     * @return set with all columns to use
     */
    private <E extends Column<?, C>> Set<E> removeSkippedColumns(Collection<E> cols) {
        return cols.stream()
            .filter(column -> column.isColumnUsed(context))
            .collect(Collectors.toSet());
    }

    @SafeVarargs
    private final Set<Column<?, C>> removeSkippedColumns(Column<?, C>... cols) {
        return removeSkippedColumns(Arrays.asList(cols));
    }
}
