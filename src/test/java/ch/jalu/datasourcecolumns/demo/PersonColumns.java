package ch.jalu.datasourcecolumns.demo;

import ch.jalu.datasourcecolumns.Column;
import ch.jalu.datasourcecolumns.ColumnType;
import ch.jalu.datasourcecolumns.StandardTypes;

public final class PersonColumns<T> implements Column<T, Configuration> {

    public static final PersonColumns<String> NAME =
        new PersonColumns<>(StandardTypes.STRING, "name");

    public static final PersonColumns<String> LOCATION =
        new PersonColumns<>(StandardTypes.STRING, "location");

    public static final PersonColumns<Integer> AGE =
        new PersonColumns<>(StandardTypes.INTEGER, "age");

    public static final PersonColumns<Long> WEIGHT =
        new PersonColumns<>(StandardTypes.LONG, "weight");

    public static final PersonColumns<Boolean> IS_HOME =
        new PersonColumns<>(StandardTypes.BOOLEAN, "isHome");


    private final ColumnType<T> type;
    private final String id;

    private PersonColumns(ColumnType<T> type, String id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public String resolveName(Configuration context) {
        return context.getColumnName(id);
    }

    @Override
    public ColumnType<T> getType() {
        return type;
    }

    @Override
    public boolean isColumnUsed(Configuration context) {
        return !context.shouldBeSkipped(id);
    }

    @Override
    public boolean useDefaultForNullValue(Configuration context) {
        return false;
    }

    @Override
    public String toString() {
        return "PersonColumns[" + id + "]";
    }

    public String getColumnId() {
        return id;
    }
}