package io.storedmapper.dialect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MySqlDialectTest {

    private MySqlDialect dialect;

    @BeforeEach
    void setUp() {
        dialect = new MySqlDialect();
    }

    @Test
    void formatFullName_shouldQuoteWithBackticks() {
        assertEquals("`mydb`.`sp_get_users`", dialect.formatFullName("mydb", "sp_get_users"));
    }

    @Test
    void createTableFunctionQuery_shouldGenerateSelectFrom() {
        var sql = dialect.createTableFunctionQuery(
                "`mydb`.`fn_get_users`", List.of("?", "?"), null);
        assertEquals("SELECT * FROM `mydb`.`fn_get_users`(?,?)", sql);
    }

    @Test
    void createTableFunctionQuery_shouldIncludeOrderBy() {
        var sql = dialect.createTableFunctionQuery(
                "`mydb`.`fn_get_users`", List.of("?"), "id ASC");
        assertEquals("SELECT * FROM `mydb`.`fn_get_users`(?) ORDER BY id ASC", sql);
    }

    @Test
    void createScalarFunctionQuery_shouldGenerateSelectFunction() {
        var sql = dialect.createScalarFunctionQuery(
                "`mydb`.`fn_count`", List.of("?"));
        assertEquals("SELECT `mydb`.`fn_count`(?)", sql);
    }

    @Test
    void createStoredProcedureCall_shouldGenerateCallStatement() {
        var sql = dialect.createStoredProcedureCall(
                "`mydb`.`sp_update`", List.of("?", "?"));
        assertEquals("CALL `mydb`.`sp_update`(?,?)", sql);
    }
}
