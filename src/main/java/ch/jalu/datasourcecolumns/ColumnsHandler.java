package ch.jalu.datasourcecolumns;

import ch.jalu.datasourcecolumns.data.DataSourceValue;
import ch.jalu.datasourcecolumns.data.DataSourceValues;
import ch.jalu.datasourcecolumns.data.UpdateValues;
import ch.jalu.datasourcecolumns.predicate.Predicate;

import java.util.List;

/**
 * Handler which performs operations on the data source based on the given
 * columns and values.
 *
 * @param <C> the context type
 * @param <I> the identifier type
 */
@SuppressWarnings("unchecked")
public interface ColumnsHandler<C, I> {

    /**
     * Retrieves the given column from a given row.
     *
     * @param identifier the id of the row to look up
     * @param column the column whose value should be retrieved
     * @param <T> the column type
     * @return the result of the lookup
     */
    <T> DataSourceValue<T> retrieve(I identifier, Column<T, C> column) throws Exception;

    /**
     * Retrieves multiple values from a given row.
     *
     * @param identifier the id of the row to look up
     * @param columns the columns to retrieve
     * @return map-like object with the requested values
     */
    DataSourceValues retrieve(I identifier, Column<?, C>... columns) throws Exception;

    /**
     * Retrieves a column's value for all rows that satisfy the given predicate.
     *
     * @param predicate the predicate to fulfill
     * @param column the column to retrieve from the matching rows
     * @param <T> the column's value type
     * @return the values of the matching rows
     */
    <T> List<T> retrieve(Predicate<C> predicate, Column<T, C> column) throws Exception;

    /**
     * Retrieves the given columns' values from all rows that satisfy the given predicate.
     *
     * @param predicate the predicate to fulfill
     * @param columns the columns to retrieve from the matching rows
     * @return the values in the matching rows
     */
    List<DataSourceValues> retrieve(Predicate<C> predicate, Column<?, C>... columns) throws Exception;

    /**
     * Changes a column from a specific row to the given value.
     *
     * @param identifier the id of the row to modify
     * @param column the column to modify
     * @param value the value to set the column to
     * @param <T> the column type
     * @return true upon success, false otherwise
     */
    <T> boolean update(I identifier, Column<T, C> column, T value) throws Exception;

    /**
     * Updates a row to have the given values.
     *
     * @param identifier the id of the row to modify
     * @param updateValues the values to set on the row
     * @return true upon success, false otherwise
     */
    boolean update(I identifier, UpdateValues<C> updateValues) throws Exception;

    /**
     * Updates a row to have the values as retrieved from the dependent object.
     *
     * @param identifier the id of the row to modify
     * @param dependent the dependent to get values from
     * @param columns the columns to update in the row
     * @param <D> the dependent type
     * @return true upon success, false otherwise
     */
    <D> boolean update(I identifier, D dependent, DependentColumn<?, C, D>... columns) throws Exception;

    /**
     * Sets the given value to the provided column for all rows which match the predicate.
     *
     * @param predicate the predicate to filter rows by
     * @param column the column to modify on the matched rows
     * @param value the new value to set
     * @param <T> the column type
     * @return number of modified rows
     */
    <T> int update(Predicate<C> predicate, Column<T, C> column, T value) throws Exception;

    /**
     * Updates all rows that match the given predicate with the provided values.
     *
     * @param predicate the predicate to filter rows by
     * @param updateValues the values to set on the matched rows
     * @return number of modified rows
     */
    int update(Predicate<C> predicate, UpdateValues<C> updateValues) throws Exception;

    /**
     * Inserts the given values into a new row.
     *
     * @param updateValues the values to insert
     * @return true upon success, false otherwise
     * @throws IllegalStateException if there is not at least one column that is not skipped
     */
    boolean insert(UpdateValues<C> updateValues) throws Exception;

    /**
     * Inserts the given values into a new row, as taken from the dependent object.
     *
     * @param dependent the dependent to get values from
     * @param columns the columns to insert
     * @param <D> the dependent type
     * @return true upon success, false otherwise
     * @throws IllegalStateException if there is not at least one column that is not skipped
     */
    <D> boolean insert(D dependent, DependentColumn<?, C, D>... columns) throws Exception;

    /**
     * Returns the number of rows that match the provided predicate.
     *
     * @param predicate the predicate to test the rows for
     * @return number of rows fulfilling the predicate
     */
    int count(Predicate<C> predicate) throws Exception;

}
