package io.storedmapper.dialect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostgreSqlDialectTest {

    private PostgreSqlDialect dialect;

    @BeforeEach
    void setUp() {
        dialect = new PostgreSqlDialect();
    }

    @Test
    void formatFullName_shouldQuoteWithDoubleQuotes() {
        assertEquals("\"public\".\"sp_get_tasks\"", dialect.formatFullName("public", "sp_get_tasks"));
    }

    @Test
    void createTableFunctionQuery_shouldGenerateSelectFromWithParams() {
        var sql = dialect.createTableFunctionQuery(
                "\"public\".\"fn_get_tasks\"", List.of("?", "?", "?"), null);
        assertEquals("SELECT * FROM \"public\".\"fn_get_tasks\"(?,?,?)", sql);
    }

    @Test
    void createTableFunctionQuery_shouldIncludeOrderBy() {
        var sql = dialect.createTableFunctionQuery(
                "\"public\".\"fn_get_tasks\"", List.of("?"), "id ASC");
        assertEquals("SELECT * FROM \"public\".\"fn_get_tasks\"(?) ORDER BY id ASC", sql);
    }

    @Test
    void createTableFunctionQuery_shouldOmitOrderByWhenBlank() {
        var sql = dialect.createTableFunctionQuery(
                "\"public\".\"fn_get_tasks\"", List.of("?"), "  ");
        assertEquals("SELECT * FROM \"public\".\"fn_get_tasks\"(?)", sql);
    }

    @Test
    void createTableFunctionQuery_withNoParams() {
        var sql = dialect.createTableFunctionQuery(
                "\"public\".\"fn_get_all\"", List.of(), null);
        assertEquals("SELECT * FROM \"public\".\"fn_get_all\"()", sql);
    }

    @Test
    void createScalarFunctionQuery_shouldGenerateSelectFunction() {
        var sql = dialect.createScalarFunctionQuery(
                "\"public\".\"fn_count\"", List.of("?"));
        assertEquals("SELECT \"public\".\"fn_count\"(?)", sql);
    }

    @Test
    void createStoredProcedureCall_shouldGenerateCallStatement() {
        var sql = dialect.createStoredProcedureCall(
                "\"public\".\"sp_update\"", List.of("?", "?"));
        assertEquals("CALL \"public\".\"sp_update\"(?,?)", sql);
    }
}
