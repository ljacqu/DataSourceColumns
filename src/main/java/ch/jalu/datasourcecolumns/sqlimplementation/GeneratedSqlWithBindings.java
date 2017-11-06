package ch.jalu.datasourcecolumns.sqlimplementation;

import java.util.List;

public class GeneratedSqlWithBindings {

    private final String generatedSql;
    private final List<Object> bindings;

    public GeneratedSqlWithBindings(String generatedSql, List<Object> bindings) {
        this.generatedSql = generatedSql;
        this.bindings = bindings;
    }

    public String getGeneratedSql() {
        return generatedSql;
    }

    public List<Object> getBindings() {
        return bindings;
    }
}
