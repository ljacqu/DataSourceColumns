package ch.jalu.datasourcecolumns.sqlimplementation;

import java.util.List;

/**
 * Contains generated SQL code and values to be bound to the placeholders
 * in the SQL code.
 */
public class GeneratedSqlWithBindings {

    private final String generatedSql;
    private final List<Object> bindings;

    /**
     * Constructor.
     *
     * @param generatedSql the generated SQL code
     * @param bindings the values to bind to the placeholders
     */
    public GeneratedSqlWithBindings(String generatedSql, List<Object> bindings) {
        this.generatedSql = generatedSql;
        this.bindings = bindings;
    }

    /**
     * @return the generated SQL
     */
    public String getGeneratedSql() {
        return generatedSql;
    }

    /**
     * @return the bindings to add to the generated SQL
     */
    public List<Object> getBindings() {
        return bindings;
    }
}
