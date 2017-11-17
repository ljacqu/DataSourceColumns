package ch.jalu.datasourcecolumns.demo;

import ch.jalu.datasourcecolumns.ColumnsHandler;
import ch.jalu.datasourcecolumns.data.DataSourceValues;
import ch.jalu.datasourcecolumns.data.UpdateValues;
import ch.jalu.datasourcecolumns.sqlimplementation.SqlColumnsHandler;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.eq;
import static ch.jalu.datasourcecolumns.predicate.StandardPredicates.greaterThan;

/**
 * Small demo of the {@code datasourcecolumns} project.
 * <p>
 * Performs the same action multiple times with different configurations for illustration.
 * You can find the configurations in {@code test/resources/demo}. The loaded properties
 * file configures {@link PersonColumns} properties, which will be used to interact with the database.
 */
public class DemoRunner {

    public static void main(String... args) throws Exception {
        runDemoForConfig("config1");
        runDemoForConfig("config2");
        runDemoForConfig("config3");
    }

    private static void runDemoForConfig(String configName) throws Exception {
        // Load config & create database
        Configuration configuration = Configuration.loadConfig("/demo/" + configName + ".properties");
        Connection connection = DemoDatabaseInitializer.createH2InMemoryDatabase(configName, configuration);

        // Create columns handler
        String idName = PersonColumns.NAME.resolveName(configuration);
        ColumnsHandler<Configuration, String> columnsHandler =
            new SqlColumnsHandler<>(connection, configuration, "tbl", idName);

        // Run demo!
        System.out.println();
        System.out.println("--------------- Demo with config " + configName + " ---------------");
        performDatabaseOperations(columnsHandler);
    }

    /*
     * Performs various operations on a database and outputs the results.
     * <p>
     * First some entries are inserted into the database and then the data is queried and modified.
     */
    private static void performDatabaseOperations(ColumnsHandler<Configuration, String> columnsHandler)
        throws Exception {

        // Insert some rows
        for (UpdateValues<Configuration> record : createRecords()) {
            columnsHandler.insert(record);
        }

        // Update Charlie to be 20
        columnsHandler.update("Charlie", PersonColumns.AGE, 20);

        // Count all people who are 20
        int twentyYearOlds = columnsHandler.count(eq(PersonColumns.AGE, 20));
        System.out.println(twentyYearOlds + " match the age test");

        // Count people aged >= 18 who are not at home
        int adultsNotAtHome = columnsHandler.count(greaterThan(PersonColumns.AGE, 17)
            .and(eq(PersonColumns.IS_HOME, false)));
        System.out.println(adultsNotAtHome + " adults are not at home");

        // Update Bobby to be at home and to weigh 100
        columnsHandler.update("Bobby", UpdateValues.with(PersonColumns.IS_HOME, true)
            .and(PersonColumns.WEIGHT, 100L).build());

        // Get info on bobby
        PersonColumns<?>[] columns = { PersonColumns.AGE, PersonColumns.IS_HOME, PersonColumns.LOCATION, PersonColumns.WEIGHT };
        DataSourceValues bobbyValues = columnsHandler.retrieve("Bobby", columns);
        for (PersonColumns<?> column : columns) {
            System.out.println("Bobby->" + column.getColumnId() + ": " + bobbyValues.get(column));
        }
    }

    private static List<UpdateValues<Configuration>> createRecords() {
        UpdateValues<Configuration> bobby = UpdateValues
            .with(PersonColumns.NAME, "Bobby")
            .and(PersonColumns.AGE, 30)
            .and(PersonColumns.WEIGHT, 123L)
            .and(PersonColumns.LOCATION, "AT")
            .and(PersonColumns.IS_HOME, false).build();

        UpdateValues<Configuration> charlie = UpdateValues
            .with(PersonColumns.NAME, "Charlie")
            .and(PersonColumns.AGE, 25)
            .and(PersonColumns.WEIGHT, 89L)
            .and(PersonColumns.LOCATION, "CA")
            .and(PersonColumns.IS_HOME, true).build();

        UpdateValues<Configuration> dexter = UpdateValues
            .with(PersonColumns.NAME, "Dexter")
            .and(PersonColumns.AGE, 20)
            .and(PersonColumns.WEIGHT, 76L)
            .and(PersonColumns.LOCATION, "US")
            .and(PersonColumns.IS_HOME, false).build();

        UpdateValues<Configuration> eva = UpdateValues
            .with(PersonColumns.NAME, "Eva")
            .and(PersonColumns.AGE, 15)
            .and(PersonColumns.WEIGHT, 63L)
            .and(PersonColumns.LOCATION, "CA")
            .and(PersonColumns.IS_HOME, false).build();

        return Arrays.asList(bobby, charlie, dexter, eva);
    }
}
