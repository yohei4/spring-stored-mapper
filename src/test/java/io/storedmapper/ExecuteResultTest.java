package io.storedmapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecuteResultTest {

    @BeforeEach
    void setUp() {
        DbProgramMapperOptions.reset();
    }

    @AfterEach
    void tearDown() {
        DbProgramMapperOptions.reset();
    }

    @Test
    void isSuccess_shouldReturnTrueWhenReturnCodeIsNull() {
        var result = new ExecuteResult(1, null);
        assertTrue(result.isSuccess());
        assertFalse(result.hasError());
    }

    @Test
    void isSuccess_shouldReturnTrueWhenReturnCodeMatchesSuccessCode() {
        var result = new ExecuteResult(1, 0);
        assertTrue(result.isSuccess());
        assertFalse(result.hasError());
    }

    @Test
    void hasError_shouldReturnTrueWhenReturnCodeIsNonZero() {
        var result = new ExecuteResult(0, 1);
        assertTrue(result.hasError());
        assertFalse(result.isSuccess());
    }

    @Test
    void isNotFoundError_shouldMatchConfiguredErrorCode() {
        DbProgramMapperOptions.configure(config -> {
            var codes = new DbErrorCodes();
            codes.setNotFound(404);
            config.setErrorCodes(codes);
        });

        var result = new ExecuteResult(0, 404);
        assertTrue(result.isNotFoundError());
        assertTrue(result.hasError());
    }

    @Test
    void isNotFoundError_shouldReturnFalseWhenNotConfigured() {
        var result = new ExecuteResult(0, 1);
        assertFalse(result.isNotFoundError());
    }

    @Test
    void isDuplicateError_shouldMatchConfiguredErrorCode() {
        DbProgramMapperOptions.configure(config -> {
            var codes = new DbErrorCodes();
            codes.setDuplicate(2);
            config.setErrorCodes(codes);
        });

        var result = new ExecuteResult(0, 2);
        assertTrue(result.isDuplicateError());
    }

    @Test
    void isOptimisticLockError_shouldMatchConfiguredErrorCode() {
        DbProgramMapperOptions.configure(config -> {
            var codes = new DbErrorCodes();
            codes.setOptimisticLock(3);
            config.setErrorCodes(codes);
        });

        var result = new ExecuteResult(0, 3);
        assertTrue(result.isOptimisticLockError());
    }

    @Test
    void isExclusiveLockError_shouldMatchConfiguredErrorCode() {
        DbProgramMapperOptions.configure(config -> {
            var codes = new DbErrorCodes();
            codes.setExclusiveLock(10);
            config.setErrorCodes(codes);
        });

        var result = new ExecuteResult(0, 10);
        assertTrue(result.isExclusiveLockError());
    }

    @Test
    void errorChecks_shouldReturnFalseWhenReturnCodeIsNull() {
        var result = new ExecuteResult(0, null);
        assertFalse(result.isNotFoundError());
        assertFalse(result.isDuplicateError());
        assertFalse(result.isExclusiveLockError());
        assertFalse(result.isDeadlockError());
    }

    @Test
    void isSuccess_withCustomSuccessCode() {
        DbProgramMapperOptions.configure(config -> {
            var codes = new DbErrorCodes();
            codes.setSuccess(200);
            config.setErrorCodes(codes);
        });

        var successResult = new ExecuteResult(0, 200);
        assertTrue(successResult.isSuccess());

        var errorResult = new ExecuteResult(0, 0);
        assertTrue(errorResult.hasError());
    }
}
