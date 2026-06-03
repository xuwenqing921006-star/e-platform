package com.centralbank.eplatform.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class CentralBankSchemaTest
{
    @Test
    void initializesBusinessTablesInIsolatedH2Database() throws Exception
    {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:central_bank_t008;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", ""))
        {
            runSql(connection, "sql/central_bank_schema_h2.sql");
            runSql(connection, "sql/central_bank_seed_h2.sql");

            assertThat(count(connection, "cb_content")).isEqualTo(4);
            assertThat(count(connection, "cb_attachment")).isEqualTo(4);
            assertThat(count(connection, "cb_financial_product")).isEqualTo(10);
            assertThat(count(connection, "cb_account_extension")).isEqualTo(2);
        }
    }

    private static void runSql(Connection connection, String resourcePath) throws Exception
    {
        String sql = new ClassPathResource(resourcePath).getContentAsString(StandardCharsets.UTF_8);
        try (Statement statement = connection.createStatement())
        {
            for (String command : sql.split(";"))
            {
                String trimmed = command.trim();
                if (!trimmed.isEmpty())
                {
                    statement.execute(trimmed);
                }
            }
        }
    }

    private static int count(Connection connection, String tableName) throws Exception
    {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("select count(*) from " + tableName))
        {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}
