package ch.jalu.datasourcecolumns.sqlimplementation;

import ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator.ConnectionSupplier;
import ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator.PreparedStatementGeneratorFactory;

import java.sql.Connection;

/**
 * Configuration for creating {@link SqlColumnsHandler SQL column handlers}.
 *
 * @param <C> the context type
 * @see #forSingleConnection
 * @see #forConnectionPool
 */
public class SqlColumnsHandlerConfig<C> {

    private final String tableName;
    private final String idColumn;
    private final C context;
    private PreparedStatementGeneratorFactory statementGeneratorFactory;
    private ResultSetValueRetriever<C> resultSetValueRetriever;
    private PredicateSqlGenerator<C> predicateSqlGenerator;

    protected SqlColumnsHandlerConfig(String tableName, String idColumn, C context,
                                      PreparedStatementGeneratorFactory generatorFactory) {
        this.tableName = tableName;
        this.idColumn = idColumn;
        this.context = context;
        this.statementGeneratorFactory = generatorFactory;
        this.resultSetValueRetriever = new ResultSetValueRetriever<>(context);
        this.predicateSqlGenerator = new PredicateSqlGenerator<>(context);
    }

    /**
     * Creates a configuration with defaults based on a single, long-living Connection that should be used.
     *
     * @param connection the connection to the database to use
     * @param tableName the table name
     * @param idColumn name of the ID column
     * @param context the context
     * @param <C> the context type
     * @return configuration with defaults
     */
    public static <C> SqlColumnsHandlerConfig<C> forSingleConnection(Connection connection,
                                                                     String tableName, String idColumn, C context) {
        PreparedStatementGeneratorFactory statementGeneratorFactory =
            PreparedStatementGeneratorFactory.fromConnection(connection);
        return new SqlColumnsHandlerConfig<>(tableName, idColumn, context, statementGeneratorFactory);
    }

    /**
     * Creates a configuration with defaults for the given connection supplier. Connections will be retrieved from
     * the given supplier and they will be closed after each operation.
     *
     * @param connectionSupplier supplier of connections to the database
     * @param tableName the table name
     * @param idColumn name of the ID column
     * @param context the context
     * @param <C> the context type
     * @return configuration with defaults
     */
    public static <C> SqlColumnsHandlerConfig<C> forConnectionPool(ConnectionSupplier connectionSupplier,
                                                                   String tableName, String idColumn, C context) {
        PreparedStatementGeneratorFactory statementGeneratorFactory =
            PreparedStatementGeneratorFactory.fromConnectionPool(connectionSupplier);
        return new SqlColumnsHandlerConfig<>(tableName, idColumn, context, statementGeneratorFactory);
    }

    /**
     * Sets a custom {@link PreparedStatementGeneratorFactory}.
     *
     * @param factory factory to set
     * @return this instance
     */
    public SqlColumnsHandlerConfig<C> setPreparedStatementGeneratorFactory(PreparedStatementGeneratorFactory factory) {
        this.statementGeneratorFactory = factory;
        return this;
    }

    /**
     * Sets a custom {@link ResultSetValueRetriever}.
     *
     * @param resultSetValueRetriever value retriever to set
     * @return this instance
     */
    public SqlColumnsHandlerConfig<C> setResultSetValueRetriever(ResultSetValueRetriever<C> resultSetValueRetriever) {
        this.resultSetValueRetriever = resultSetValueRetriever;
        return this;
    }

    /**
     * Sets a custom {@link PredicateSqlGenerator}.
     *
     * @param predicateSqlGenerator predicate SQL generator to set
     * @return this instance
     */
    public SqlColumnsHandlerConfig<C> setPredicateSqlGenerator(PredicateSqlGenerator<C> predicateSqlGenerator) {
        this.predicateSqlGenerator = predicateSqlGenerator;
        return this;
    }

    // ----------
    // Getters
    // ----------

    public String getTableName() {
        return tableName;
    }

    public String getIdColumn() {
        return idColumn;
    }

    public C getContext() {
        return context;
    }

    public PreparedStatementGeneratorFactory getStatementGeneratorFactory() {
        return statementGeneratorFactory;
    }

    public ResultSetValueRetriever<C> getResultSetValueRetriever() {
        return resultSetValueRetriever;
    }

    public PredicateSqlGenerator<C> getPredicateSqlGenerator() {
        return predicateSqlGenerator;
    }
}
