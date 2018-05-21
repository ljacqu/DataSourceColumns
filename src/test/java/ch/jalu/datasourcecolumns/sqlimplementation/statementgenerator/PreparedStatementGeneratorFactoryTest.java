package ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator;

import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link PreparedStatementGeneratorFactory}.
 */
public class PreparedStatementGeneratorFactoryTest {

    @Test
    public void shouldCreateFromConnectionObject() throws SQLException {
        // given
        Connection connection = mock(Connection.class);
        PreparedStatement pst = mock(PreparedStatement.class);
        String sql = "SELECT 1 FROM table WHERE name = ?";
        given(connection.prepareStatement(sql)).willReturn(pst);
        PreparedStatementGeneratorFactory generatorFactory = PreparedStatementGeneratorFactory.fromConnection(connection);

        // when
        PreparedStatementGenerator generator = generatorFactory.create(sql);

        // then
        assertThat(generator, instanceOf(SingleConnectionPreparedStatementGenerator.class));
        assertThat(generator.createStatement(), equalTo(pst));
    }

    @Test
    public void shouldCreateFromConnectionPool() throws SQLException {
        // given
        Connection connection = mock(Connection.class);
        PreparedStatement pst = mock(PreparedStatement.class);
        String sql = "SELECT 1 FROM table";
        given(connection.prepareStatement(sql)).willReturn(pst);
        ConnectionSupplier supplier = () -> connection;
        PreparedStatementGeneratorFactory generatorFactory = PreparedStatementGeneratorFactory.fromConnectionPool(supplier);

        // when
        PreparedStatementGenerator generator = generatorFactory.create(sql);

        // then
        assertThat(generator, instanceOf(ConnectionPoolPreparedStatementGenerator.class));
        assertThat(generator.createStatement(), equalTo(pst));
    }
}