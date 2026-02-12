package io.storedmapper;

import io.storedmapper.dialect.MySqlDialect;
import io.storedmapper.dialect.PostgreSqlDialect;
import io.storedmapper.dialect.SqlServerDialect;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DbProgramMapperOptionsTest {

    @AfterEach
    void tearDown() {
        DbProgramMapperOptions.reset();
    }

    @Test
    void defaults_shouldBeSqlServerAndDbo() {
        assertInstanceOf(SqlServerDialect.class, DbProgramMapperOptions.getDialect());
        assertEquals("dbo", DbProgramMapperOptions.getDefaultSchema());
        assertNotNull(DbProgramMapperOptions.getErrorCodes());
    }

    @Test
    void configure_shouldUpdateDialect() {
        DbProgramMapperOptions.configure(config -> {
            config.setDialect(new PostgreSqlDialect());
        });
        assertInstanceOf(PostgreSqlDialect.class, DbProgramMapperOptions.getDialect());
    }

    @Test
    void configure_shouldUpdateDefaultSchema() {
        DbProgramMapperOptions.configure(config -> {
            config.setDefaultSchema("public");
        });
        assertEquals("public", DbProgramMapperOptions.getDefaultSchema());
    }

    @Test
    void configure_shouldUpdateErrorCodes() {
        var codes = new DbErrorCodes();
        codes.setNotFound(404);
        codes.setDuplicate(409);

        DbProgramMapperOptions.configure(config -> {
            config.setErrorCodes(codes);
        });

        assertEquals(404, DbProgramMapperOptions.getErrorCodes().getNotFound());
        assertEquals(409, DbProgramMapperOptions.getErrorCodes().getDuplicate());
    }

    @Test
    void configure_shouldNotOverrideUnsetValues() {
        DbProgramMapperOptions.configure(config -> {
            config.setDialect(new MySqlDialect());
        });

        // Schema should remain unchanged
        assertEquals("dbo", DbProgramMapperOptions.getDefaultSchema());
        assertInstanceOf(MySqlDialect.class, DbProgramMapperOptions.getDialect());
    }

    @Test
    void reset_shouldRestoreDefaults() {
        DbProgramMapperOptions.configure(config -> {
            config.setDialect(new PostgreSqlDialect());
            config.setDefaultSchema("custom");
        });

        DbProgramMapperOptions.reset();

        assertInstanceOf(SqlServerDialect.class, DbProgramMapperOptions.getDialect());
        assertEquals("dbo", DbProgramMapperOptions.getDefaultSchema());
    }
}
