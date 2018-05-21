package ch.jalu.datasourcecolumns.sqlimplementation.statementgenerator;

import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Test for {@link PreparedStatementGenerator}.
 */
public class PreparedStatementGeneratorTest {

    @Test
    public void shouldCreateFromConnectionObject() throws SQLException {
        // given
        Connection connection = mock(Connection.class);
        PreparedStatement pst = mock(PreparedStatement.class);
        given(connection.prepareStatement(anyString())).willReturn(pst);
        PreparedStatementGenerator statementGenerator = PreparedStatementGenerator.fromConnection(connection);
        String sql = "SELECT 1 FROM table WHERE name = ?";

        // when
        PreparedStatementResult result = statementGenerator.create(sql);

        // then
        verify(connection).prepareStatement(sql);
        assertThat(result.getPreparedStatement(), equalTo(pst));
    }
}