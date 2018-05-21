package ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator;

import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Test for {@link ConnectionPoolPreparedStatementGenerator}.
 */
public class ConnectionPoolPreparedStatementGeneratorTest {

    @Test
    public void shouldCreateOnlyOnePreparedStatement() throws SQLException {
        // given
        Connection connection = mock(Connection.class);
        given(connection.prepareStatement(anyString())).willReturn(mock(PreparedStatement.class));
        ConnectionSupplier connectionSupplier = mock(ConnectionSupplier.class);
        given(connectionSupplier.get()).willReturn(connection);
        String sql = "my sql code";
        ConnectionPoolPreparedStatementGenerator generator = new ConnectionPoolPreparedStatementGenerator(connectionSupplier, sql);

        // when
        PreparedStatement statement1 = generator.createStatement();
        PreparedStatement statement2 = generator.createStatement();
        generator.close();

        // then
        assertThat(statement1, not(nullValue()));
        assertThat(statement1, sameInstance(statement2));
        verify(connectionSupplier, only()).get();
        verify(connection).prepareStatement(sql);
        verify(connection).close();
        verify(statement1).close();
        verifyNoMoreInteractions(connection);
    }

    @Test
    public void shouldHandleNullStatement() throws SQLException {
        // given
        String sql = "sql code";
        ConnectionSupplier connectionSupplier = mock(ConnectionSupplier.class);
        ConnectionPoolPreparedStatementGenerator generator = new ConnectionPoolPreparedStatementGenerator(connectionSupplier, sql);

        // when
        generator.close();

        // then
        verifyZeroInteractions(connectionSupplier);
    }


    @Test
    public void shouldCreateFromConnectionPool() throws SQLException {
        // given
        Connection connection = mock(Connection.class);
        given(connection.prepareStatement(anyString())).willReturn(mock(PreparedStatement.class));
        ConnectionSupplier supplier = () -> connection;
        String sql = "SELECT 1 FROM table";

        // when
        PreparedStatement statement;
        try (PreparedStatementGenerator generator = new ConnectionPoolPreparedStatementGenerator(supplier, sql)) {
            statement = generator.createStatement();
        }

        // then
        assertThat(statement, not(nullValue()));
        verify(connection).prepareStatement(sql);
        verify(connection).close();
        verify(statement).close();
    }

    @Test
    public void shouldCloseConnectionIfPreparedStatementInitFails() throws SQLException {
        // given
        Connection connection = mock(Connection.class);
        given(connection.prepareStatement(anyString())).willThrow(SQLException.class);
        ConnectionSupplier supplier = () -> connection;
        String sql = "SELECT 1 FROM table";

        // when
        try (PreparedStatementGenerator generator = new ConnectionPoolPreparedStatementGenerator(supplier, sql)) {
            generator.createStatement();

            // then
            fail("expected exception to be thrown");
        } catch (SQLException ignore) {
        }

        verify(connection).prepareStatement(sql);
        verify(connection).close();
    }

    @Test
    public void shouldClosePreparedStatementEvenIfClosingConnectionFails() throws SQLException {
        // given
        Connection connection = mock(Connection.class);
        given(connection.prepareStatement(anyString())).willReturn(mock(PreparedStatement.class));
        doThrow(SQLException.class).when(connection).close();
        ConnectionSupplier supplier = () -> connection;
        String sql = "SELECT 1 FROM table";

        // when
        PreparedStatement statement = null;
        try {
            try (PreparedStatementGenerator generator = new ConnectionPoolPreparedStatementGenerator(supplier, sql)) {
                statement = generator.createStatement();
            }

            // then
            fail("Expected exception to be thrown (by Connection#close)");
        } catch (SQLException ignore) {
        }

        verify(connection).prepareStatement(sql);
        verify(connection).close();
        verify(statement).close();
    }

    @Test
    public void shouldCloseConnectionEvenIfClosingPreparedStatementFails() throws SQLException {
        // given
        Connection connection = mock(Connection.class);
        PreparedStatement pst = mock(PreparedStatement.class);
        given(connection.prepareStatement(anyString())).willReturn(pst);
        doThrow(SQLException.class).when(pst).close();
        ConnectionSupplier supplier = () -> connection;
        String sql = "SELECT 1 FROM table";

        // when
        PreparedStatement statement = null;
        try {
            try (PreparedStatementGenerator generator = new ConnectionPoolPreparedStatementGenerator(supplier, sql)) {
                statement = generator.createStatement();
            }

            // then
            fail("Expected exception to be thrown (by PreparedStatement#close)");
        } catch (SQLException ignore) {
        }

        verify(connection).prepareStatement(sql);
        verify(connection).close();
        verify(statement).close();
    }
}