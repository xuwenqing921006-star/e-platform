package com.centralbank.eplatform.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class T019InitialDataClosureTest
{
    @Test
    void seedScriptsAreRepeatableAndCreateInitialAccountExtensions() throws Exception
    {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:central_bank_t019;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", ""))
        {
            runSql(connection, "sql/central_bank_schema_h2.sql");
            runSql(connection, "sql/central_bank_seed_h2.sql");
            runSql(connection, "sql/central_bank_seed_h2.sql");

            assertThat(count(connection, "cb_account_extension")).isEqualTo(2);
            assertThat(queryString(connection,
                    "select role from cb_account_extension where user_id = 1")).isEqualTo("ADMIN");
            assertThat(queryString(connection,
                    "select office_code from cb_account_extension where user_id = 2"))
                            .isEqualTo("MONETARY_CREDIT");
            assertThat(count(connection, "cb_financial_product")).isEqualTo(9);
            assertThat(countWhere(connection, "cb_financial_product", "product_type = 'AGRICULTURAL'"))
                    .isEqualTo(4);
            assertThat(countWhere(connection, "cb_financial_product", "product_type = 'SMALL_MICRO'"))
                    .isEqualTo(5);
            assertThat(countWhere(connection, "cb_financial_product",
                    "bank_code in ('CCB', 'CITIC', 'LONGJIANG_BANK', "
                            + "'KUNLUN_BANK', 'ICBC', 'ABC', 'BOC', 'HARBIN_BANK')"))
                    .isEqualTo(9);
            assertThat(countWhere(connection, "cb_financial_product",
                    "bank_code = 'SUNSHINE_AGRICULTURE' or product_name = '阳光惠农贷'"))
                    .isEqualTo(0);
            assertThat(queryString(connection,
                    "select bank_name from cb_financial_product where product_name = '农兴贷'"))
                            .isEqualTo("哈尔滨银行大庆分行");
        }
    }

    @Test
    void reportDocumentsTenSampleScopeFromProvidedRealProductSource() throws Exception
    {
        Path report = findProjectRoot().resolve(".sdd/T-019-data-source-audit.md");

        assertThat(report).exists();
        String content = Files.readString(report, StandardCharsets.UTF_8);
        assertThat(content).contains("数据源已提供");
        assertThat(content).contains("已抽取 10 条真实样本");
        assertThat(content).contains("不声明 112 条全量已初始化");
        assertThat(content).doesNotContain("BLOCKED");
    }

    private static Path findProjectRoot()
    {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (current != null)
        {
            if (Files.isDirectory(current.resolve(".sdd")))
            {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Project root with .sdd directory not found");
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

    private static int countWhere(Connection connection, String tableName, String whereClause) throws Exception
    {
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select count(*) from " + tableName
                        + " where " + whereClause))
        {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private static String queryString(Connection connection, String sql) throws Exception
    {
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql))
        {
            resultSet.next();
            return resultSet.getString(1);
        }
    }
}
