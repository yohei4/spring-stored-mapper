package io.storedmapper.dialect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqlServerDialectTest {

    private SqlServerDialect dialect;

    @BeforeEach
    void setUp() {
        dialect = new SqlServerDialect();
    }

    @Test
    void formatFullName_shouldQuoteWithBrackets() {
        assertEquals("[dbo].[sp_get_users]", dialect.formatFullName("dbo", "sp_get_users"));
    }

    @Test
    void createTableFunctionQuery_shouldGenerateSelectFrom() {
        var sql = dialect.createTableFunctionQuery(
                "[dbo].[fn_get_users]", List.of("?", "?"), null);
        assertEquals("SELECT * FROM [dbo].[fn_get_users](?,?)", sql);
    }

    @Test
    void createTableFunctionQuery_shouldIncludeOrderBy() {
        var sql = dialect.createTableFunctionQuery(
                "[dbo].[fn_get_users]", List.of("?"), "name DESC");
        assertEquals("SELECT * FROM [dbo].[fn_get_users](?) ORDER BY name DESC", sql);
    }

    @Test
    void createScalarFunctionQuery_shouldGenerateSelectFunction() {
        var sql = dialect.createScalarFunctionQuery(
                "[dbo].[fn_count]", List.of("?"));
        assertEquals("SELECT [dbo].[fn_count](?)", sql);
    }

    @Test
    void createStoredProcedureCall_shouldGenerateJdbcCallSyntax() {
        var sql = dialect.createStoredProcedureCall(
                "[dbo].[sp_update]", List.of("?", "?"));
        assertEquals("{call [dbo].[sp_update](?,?)}", sql);
    }
}
