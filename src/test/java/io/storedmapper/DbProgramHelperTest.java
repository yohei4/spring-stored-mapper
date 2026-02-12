package io.storedmapper;

import io.storedmapper.annotation.DbParameterName;
import io.storedmapper.annotation.DbParameterOrder;
import io.storedmapper.annotation.DbProgramName;
import io.storedmapper.dialect.PostgreSqlDialect;
import io.storedmapper.internal.DbProgramHelper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DbProgramHelperTest {

    @BeforeEach
    void setUp() {
        DbProgramMapperOptions.reset();
    }

    @AfterEach
    void tearDown() {
        DbProgramMapperOptions.reset();
    }

    // --- テスト用パラメータクラス ---

    @DbProgramName("sp_get_tasks")
    static class GetTasksParam extends DbProgramBase {
        @DbParameterOrder(1) private UUID userId;
        @DbParameterOrder(2) private Integer limit;
        @DbParameterOrder(3) private Integer offset;

        GetTasksParam(UUID userId, int limit, int offset) {
            this.userId = userId;
            this.limit = limit;
            this.offset = offset;
        }
    }

    @DbProgramName(value = "sp_update_user", schema = "sales")
    static class UpdateUserParam extends DbProgramWithErrorBase {
        @DbParameterOrder(1) private UUID userId;
        @DbParameterOrder(2) private String name;

        UpdateUserParam(UUID userId, String name) {
            this.userId = userId;
            this.name = name;
        }
    }

    @DbProgramName("sp_custom")
    static class CustomNameParam extends DbProgramBase {
        @DbParameterOrder(1)
        @DbParameterName("p_user_id")
        private UUID userId;

        CustomNameParam(UUID userId) {
            this.userId = userId;
        }
    }

    static class NoAnnotationParam extends DbProgramBase {
    }

    // --- テスト ---

    @Test
    void getDbProgramNameAnnotation_shouldReturnAnnotation() {
        var param = new GetTasksParam(UUID.randomUUID(), 10, 0);
        var annotation = DbProgramHelper.getDbProgramNameAnnotation(param);
        assertNotNull(annotation);
        assertEquals("sp_get_tasks", annotation.value());
    }

    @Test
    void getDbProgramNameAnnotation_shouldReturnNullForMissing() {
        var param = new NoAnnotationParam();
        var annotation = DbProgramHelper.getDbProgramNameAnnotation(param);
        assertNull(annotation);
    }

    @Test
    void getFullName_shouldUseDefaultSchema() {
        var param = new GetTasksParam(UUID.randomUUID(), 10, 0);
        var annotation = DbProgramHelper.getDbProgramNameAnnotation(param);
        var fullName = DbProgramHelper.getFullName(annotation);
        assertEquals("[dbo].[sp_get_tasks]", fullName);
    }

    @Test
    void getFullName_shouldUseExplicitSchema() {
        var param = new UpdateUserParam(UUID.randomUUID(), "test");
        var annotation = DbProgramHelper.getDbProgramNameAnnotation(param);
        var fullName = DbProgramHelper.getFullName(annotation);
        assertEquals("[sales].[sp_update_user]", fullName);
    }

    @Test
    void getFullName_shouldUseConfiguredDialect() {
        DbProgramMapperOptions.configure(config -> {
            config.setDialect(new PostgreSqlDialect());
            config.setDefaultSchema("public");
        });

        var param = new GetTasksParam(UUID.randomUUID(), 10, 0);
        var annotation = DbProgramHelper.getDbProgramNameAnnotation(param);
        var fullName = DbProgramHelper.getFullName(annotation);
        assertEquals("\"public\".\"sp_get_tasks\"", fullName);
    }

    @Test
    void buildParameterArray_shouldReturnOrderedValues() {
        var userId = UUID.randomUUID();
        var param = new GetTasksParam(userId, 20, 5);
        var args = DbProgramHelper.buildParameterArray(param);

        assertEquals(3, args.length);
        assertEquals(userId, args[0]);
        assertEquals(20, args[1]);
        assertEquals(5, args[2]);
    }

    @Test
    void getOrderedInputFields_shouldExcludeOutputFields() {
        var param = new UpdateUserParam(UUID.randomUUID(), "test");
        var fields = DbProgramHelper.getOrderedInputFields(param);

        // UpdateUserParam has userId, name as INPUT; sqlErrorCd, progressMessage as OUTPUT
        assertEquals(2, fields.size());
        assertEquals("userId", fields.get(0).getName());
        assertEquals("name", fields.get(1).getName());
    }

    @Test
    void getOutputFields_shouldReturnOutputFields() {
        var param = new UpdateUserParam(UUID.randomUUID(), "test");
        var outputFields = DbProgramHelper.getOutputFields(param);

        assertEquals(2, outputFields.size());
        var fieldNames = outputFields.stream().map(f -> f.getName()).toList();
        assertTrue(fieldNames.contains("sqlErrorCd"));
        assertTrue(fieldNames.contains("progressMessage"));
    }

    @Test
    void getParameterName_shouldUseDbParameterNameAnnotation() throws NoSuchFieldException {
        var param = new CustomNameParam(UUID.randomUUID());
        var fields = DbProgramHelper.getOrderedInputFields(param);
        assertEquals(1, fields.size());
        assertEquals("p_user_id", DbProgramHelper.getParameterName(fields.get(0)));
    }

    @Test
    void getParameterName_shouldFallbackToFieldName() {
        var param = new GetTasksParam(UUID.randomUUID(), 10, 0);
        var fields = DbProgramHelper.getOrderedInputFields(param);
        assertEquals("userId", DbProgramHelper.getParameterName(fields.get(0)));
    }

    @Test
    void createTableFunctionQuery_shouldGenerateCorrectSql() {
        DbProgramMapperOptions.configure(config -> {
            config.setDialect(new PostgreSqlDialect());
            config.setDefaultSchema("public");
        });

        var param = new GetTasksParam(UUID.randomUUID(), 10, 0);
        var annotation = DbProgramHelper.getDbProgramNameAnnotation(param);
        var sql = DbProgramHelper.createTableFunctionQuery(annotation, param, "created_at DESC");
        assertEquals("SELECT * FROM \"public\".\"sp_get_tasks\"(?,?,?) ORDER BY created_at DESC", sql);
    }

    @Test
    void createScalarFunctionQuery_shouldGenerateCorrectSql() {
        var param = new GetTasksParam(UUID.randomUUID(), 10, 0);
        var annotation = DbProgramHelper.getDbProgramNameAnnotation(param);
        var sql = DbProgramHelper.createScalarFunctionQuery(annotation, param);
        assertEquals("SELECT [dbo].[sp_get_tasks](?,?,?)", sql);
    }

    @Test
    void createStoredProcedureCall_shouldGenerateCorrectSql() {
        var param = new GetTasksParam(UUID.randomUUID(), 10, 0);
        var annotation = DbProgramHelper.getDbProgramNameAnnotation(param);
        var sql = DbProgramHelper.createStoredProcedureCall(annotation, param);
        assertEquals("{call [dbo].[sp_get_tasks](?,?,?)}", sql);
    }

    @Test
    void setOutputParameterValue_shouldSetFieldValue() {
        var param = new UpdateUserParam(UUID.randomUUID(), "test");
        var outputFields = DbProgramHelper.getOutputFields(param);

        for (var field : outputFields) {
            if (field.getName().equals("sqlErrorCd")) {
                DbProgramHelper.setOutputParameterValue(param, field, 1);
            } else if (field.getName().equals("progressMessage")) {
                DbProgramHelper.setOutputParameterValue(param, field, "Error occurred");
            }
        }

        assertTrue(param.hasSqlError());
        assertEquals(1, param.getSqlErrorCd());
        assertEquals("Error occurred", param.getProgressMessage());
    }
}
