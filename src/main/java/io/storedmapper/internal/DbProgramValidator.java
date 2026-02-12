package io.storedmapper.internal;

import io.storedmapper.DbProgram;
import io.storedmapper.annotation.DbParameterOrder;
import io.storedmapper.annotation.DbProgramName;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DbProgram検証用クラス。
 *
 * <p>アプリケーション起動時にDbProgramクラスの定義が正しいか検証します。</p>
 *
 * <pre>{@code
 * // 起動時に全DbProgramクラスを検証
 * DbProgramValidator.validate(GetTasksParam.class, UpdateUserParam.class);
 *
 * // 検証結果を取得して個別処理
 * var result = DbProgramValidator.validateAndGetResult(GetTasksParam.class);
 * if (!result.isValid()) {
 *     result.getErrors().forEach(e -> log.warn(e.getMessage()));
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
public final class DbProgramValidator {

    /** SQLインジェクション防止のための不正文字 */
    private static final char[] INVALID_CHARS = {';', '\'', '"', '-', '/', '*', '\\', '\n', '\r', '\t'};

    private DbProgramValidator() {
    }

    /**
     * 検証エラーコード。
     */
    public enum ValidationErrorCode {
        /** DbProgramNameアノテーションが未設定 */
        MISSING_DB_PROGRAM_NAME,
        /** プログラム名が空 */
        EMPTY_PROGRAM_NAME,
        /** スキーマ名に不正文字 */
        INVALID_SCHEMA_NAME,
        /** プログラム名に不正文字 */
        INVALID_PROGRAM_NAME,
        /** DbParameterOrderの順序が重複 */
        DUPLICATE_PARAMETER_ORDER
    }

    /**
     * 検証エラー。
     */
    public static class ValidationError {
        private final Class<?> type;
        private final ValidationErrorCode code;
        private final String message;

        public ValidationError(Class<?> type, ValidationErrorCode code, String message) {
            this.type = type;
            this.code = code;
            this.message = message;
        }

        public Class<?> getType() {
            return type;
        }

        public ValidationErrorCode getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "[" + type.getSimpleName() + "] " + message;
        }
    }

    /**
     * 検証結果。
     */
    public static class ValidationResult {
        private final List<ValidationError> errors = new ArrayList<>();

        public boolean isValid() {
            return errors.isEmpty();
        }

        public List<ValidationError> getErrors() {
            return errors;
        }

        void addError(ValidationError error) {
            errors.add(error);
        }
    }

    /**
     * 指定されたDbProgramクラスを検証し、エラーがある場合は例外をスローします。
     *
     * @param classes 検証対象のクラス
     * @throws IllegalStateException 検証エラーがある場合
     */
    @SafeVarargs
    public static void validate(Class<? extends DbProgram>... classes) {
        var result = validateAndGetResult(classes);
        if (!result.isValid()) {
            var sb = new StringBuilder("DbProgram validation error:");
            for (var error : result.getErrors()) {
                sb.append(System.lineSeparator()).append(error);
            }
            throw new IllegalStateException(sb.toString());
        }
    }

    /**
     * 指定されたDbProgramクラスを検証し、結果を返します。
     *
     * @param classes 検証対象のクラス
     * @return 検証結果
     */
    @SafeVarargs
    public static ValidationResult validateAndGetResult(Class<? extends DbProgram>... classes) {
        var result = new ValidationResult();
        for (var clazz : classes) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                validateType(clazz, result);
            }
        }
        return result;
    }

    private static void validateType(Class<?> type, ValidationResult result) {
        var annotation = type.getAnnotation(DbProgramName.class);

        // @DbProgramName未設定チェック
        if (annotation == null) {
            result.addError(new ValidationError(
                    type,
                    ValidationErrorCode.MISSING_DB_PROGRAM_NAME,
                    "DbProgramName annotation is not set."));
            return;
        }

        // プログラム名が空チェック
        var programName = annotation.value();
        if (programName == null || programName.isBlank()) {
            result.addError(new ValidationError(
                    type,
                    ValidationErrorCode.EMPTY_PROGRAM_NAME,
                    "DbProgramName value is empty."));
        }

        // プログラム名の不正文字チェック
        if (programName != null && !programName.isEmpty() && containsInvalidCharacters(programName)) {
            result.addError(new ValidationError(
                    type,
                    ValidationErrorCode.INVALID_PROGRAM_NAME,
                    "Program name '" + programName + "' contains invalid characters."));
        }

        // スキーマ名の不正文字チェック
        var schema = annotation.schema();
        if (schema != null && !schema.isEmpty() && containsInvalidCharacters(schema)) {
            result.addError(new ValidationError(
                    type,
                    ValidationErrorCode.INVALID_SCHEMA_NAME,
                    "Schema name '" + schema + "' contains invalid characters."));
        }

        // @DbParameterOrderの順序重複チェック
        validateParameterOrder(type, result);
    }

    private static void validateParameterOrder(Class<?> type, ValidationResult result) {
        Set<Integer> orderValues = new HashSet<>();
        var clazz = type;
        while (clazz != null && clazz != Object.class) {
            for (var field : clazz.getDeclaredFields()) {
                var orderAnnotation = field.getAnnotation(DbParameterOrder.class);
                if (orderAnnotation != null) {
                    var order = orderAnnotation.value();
                    if (!orderValues.add(order)) {
                        result.addError(new ValidationError(
                                type,
                                ValidationErrorCode.DUPLICATE_PARAMETER_ORDER,
                                "DbParameterOrder value " + order + " is duplicated."));
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private static boolean containsInvalidCharacters(String name) {
        for (char c : name.toCharArray()) {
            for (char invalid : INVALID_CHARS) {
                if (c == invalid) {
                    return true;
                }
            }
        }
        return false;
    }
}
