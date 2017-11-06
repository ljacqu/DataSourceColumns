package ch.jalu.datasourcecolumns;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Function;

public final class SampleColumns<T> implements DependentColumn<T, SampleContext, SampleDependent> {

    public static final SampleColumns<Integer> ID =
        new SampleColumns<>(StandardTypes.INTEGER, false, SampleDependent::getId);

    public static final SampleColumns<String> NAME =
        new SampleColumns<>(StandardTypes.STRING, false, SampleDependent::getName);

    public static final SampleColumns<String> IP =
        new SampleColumns<>(StandardTypes.STRING, false, SampleDependent::getIp);

    public static final SampleColumns<String> EMAIL =
        new SampleColumns<>(StandardTypes.STRING, true, SampleDependent::getEmail);

    public static final SampleColumns<Integer> IS_LOCKED =
        new SampleColumns<>(StandardTypes.INTEGER, true, SampleDependent::getIsLocked);

    public static final SampleColumns<Integer> IS_ACTIVE =
        new SampleColumns<>(StandardTypes.INTEGER, false, SampleDependent::getIsActive);

    public static final SampleColumns<Long> LAST_LOGIN =
        new SampleColumns<>(StandardTypes.LONG, true, SampleDependent::getLastLogin);


    // -----------------------------------
    static {
        initNameFields(); // crazy or genius?
    }

    private final ColumnType<T> type;
    private final boolean isOptional;
    private final Function<SampleDependent, T> dependentGetter;
    private String name;

    private SampleColumns(ColumnType<T> type, boolean isOptional, Function<SampleDependent, T> dependentGetter) {
        this.type = type;
        this.isOptional = isOptional;
        this.dependentGetter = dependentGetter;
    }

    @Override
    public String resolveName(SampleContext context) {
        return context.resolveName(this);
    }

    @Override
    public ColumnType<T> getType() {
        return type;
    }

    @Override
    public boolean isColumnUsed(SampleContext context) {
        return !isOptional || !resolveName(context).isEmpty();
    }

    @Override
    public boolean useDefaultForNullValue(SampleContext context) {
        return context.resolveUseDefaultForNull(this);
    }

    @Override
    public T getValueFromDependent(SampleDependent dependent) {
        return dependentGetter.apply(dependent);
    }

    @Override
    public String toString() {
        return "SampleColumns[" + name + "]";
    }

    private static void initNameFields() {
        final Class<?> clazz = SampleColumns.class;
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().equals(clazz) && Modifier.isStatic(field.getModifiers())) {
                try {
                    final SampleColumns column = (SampleColumns) field.get(null);
                    column.name = field.getName();
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }
}
