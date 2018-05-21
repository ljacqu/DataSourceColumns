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

/**
 * Test for {@link PreparedStatementGeneratorFactory}.
 */
public class PreparedStatementGeneratorFactoryTest {

    @Test
    public void shouldCreateFromConnectionObject() throws SQLException {
        // given
        Connection connection = mock(Connection.class);
        PreparedStatement pst = mock(PreparedStatement.class);
        given(connection.prepareStatement(anyString())).willReturn(pst);
        PreparedStatementGeneratorFactory generatorFactory = PreparedStatementGeneratorFactory.fromConnection(connection);
        String sql = "SELECT 1 FROM table WHERE name = ?";

        // when
        PreparedStatementGenerator generator = generatorFactory.create(sql);

        // then
        assertThat(generator.createStatement(), equalTo(pst));
    }
}