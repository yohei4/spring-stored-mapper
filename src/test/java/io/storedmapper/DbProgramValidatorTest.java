package io.storedmapper;

import io.storedmapper.annotation.DbParameterOrder;
import io.storedmapper.annotation.DbProgramName;
import io.storedmapper.internal.DbProgramValidator;
import io.storedmapper.internal.DbProgramValidator.ValidationErrorCode;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DbProgramValidatorTest {

    // --- テスト用パラメータクラス ---

    @DbProgramName("sp_valid")
    static class ValidParam extends DbProgramBase {
        @DbParameterOrder(1) private String name;
        @DbParameterOrder(2) private Integer age;
    }

    static class MissingAnnotationParam extends DbProgramBase {
        @DbParameterOrder(1) private String name;
    }

    @DbProgramName("")
    static class EmptyNameParam extends DbProgramBase {
    }

    @DbProgramName("sp_valid; DROP TABLE users")
    static class InvalidNameParam extends DbProgramBase {
    }

    @DbProgramName(value = "sp_test", schema = "schema'; DROP TABLE users")
    static class InvalidSchemaParam extends DbProgramBase {
    }

    @DbProgramName("sp_duplicate_order")
    static class DuplicateOrderParam extends DbProgramBase {
        @DbParameterOrder(1) private String name;
        @DbParameterOrder(1) private String email;
    }

    @DbProgramName(value = "sp_test", schema = "public")
    static class ValidWithSchemaParam extends DbProgramBase {
        @DbParameterOrder(1) private String name;
    }

    // --- テスト ---

    @Test
    void validate_shouldPassForValidParam() {
        assertDoesNotThrow(() -> DbProgramValidator.validate(ValidParam.class));
    }

    @Test
    void validate_shouldPassForValidParamWithSchema() {
        assertDoesNotThrow(() -> DbProgramValidator.validate(ValidWithSchemaParam.class));
    }

    @Test
    void validate_shouldThrowForMissingAnnotation() {
        var ex = assertThrows(IllegalStateException.class,
                () -> DbProgramValidator.validate(MissingAnnotationParam.class));
        assertTrue(ex.getMessage().contains("DbProgramName annotation is not set"));
    }

    @Test
    void validate_shouldThrowForEmptyName() {
        var ex = assertThrows(IllegalStateException.class,
                () -> DbProgramValidator.validate(EmptyNameParam.class));
        assertTrue(ex.getMessage().contains("empty"));
    }

    @Test
    void validate_shouldThrowForInvalidName() {
        var ex = assertThrows(IllegalStateException.class,
                () -> DbProgramValidator.validate(InvalidNameParam.class));
        assertTrue(ex.getMessage().contains("invalid characters"));
    }

    @Test
    void validate_shouldThrowForInvalidSchema() {
        var ex = assertThrows(IllegalStateException.class,
                () -> DbProgramValidator.validate(InvalidSchemaParam.class));
        assertTrue(ex.getMessage().contains("invalid characters"));
    }

    @Test
    void validate_shouldThrowForDuplicateOrder() {
        var ex = assertThrows(IllegalStateException.class,
                () -> DbProgramValidator.validate(DuplicateOrderParam.class));
        assertTrue(ex.getMessage().contains("duplicated"));
    }

    @Test
    void validateAndGetResult_shouldReturnErrorsWithoutThrowing() {
        var result = DbProgramValidator.validateAndGetResult(MissingAnnotationParam.class);
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals(ValidationErrorCode.MISSING_DB_PROGRAM_NAME, result.getErrors().get(0).getCode());
    }

    @Test
    void validateAndGetResult_shouldReturnMultipleErrors() {
        var result = DbProgramValidator.validateAndGetResult(
                MissingAnnotationParam.class, InvalidNameParam.class, DuplicateOrderParam.class);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().size() >= 3);
    }

    @Test
    void validate_shouldAcceptMultipleValidClasses() {
        assertDoesNotThrow(() -> DbProgramValidator.validate(
                ValidParam.class, ValidWithSchemaParam.class));
    }
}
