package ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator;

import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Test for {@link SingleConnectionPreparedStatementGenerator}.
 */
public class SingleConnectionPreparedStatementGeneratorTest {

    @Test
    public void shouldCreateAndCloseSingleStatement() throws SQLException {
        // given
        String sql = "sql code";
        Connection connection = mock(Connection.class);
        given(connection.prepareStatement(sql)).willReturn(mock(PreparedStatement.class));
        SingleConnectionPreparedStatementGenerator generator = new SingleConnectionPreparedStatementGenerator(connection, sql);

        // when
        PreparedStatement statement1 = generator.createStatement();
        PreparedStatement statement2 = generator.createStatement();
        generator.close();

        // then
        assertThat(statement1, not(nullValue()));
        assertThat(statement1, sameInstance(statement2));
        verify(connection, only()).prepareStatement(sql);
        verify(statement1).close();
    }

    @Test
    public void shouldHandleNullStatement() throws SQLException {
        // given
        String sql = "sql code";
        Connection connection = mock(Connection.class);
        SingleConnectionPreparedStatementGenerator generator = new SingleConnectionPreparedStatementGenerator(connection, sql);

        // when
        generator.close();

        // then
        verifyNoInteractions(connection);
    }
}