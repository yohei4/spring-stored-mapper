package io.storedmapper.internal;

import io.storedmapper.DbProgram;
import io.storedmapper.DbProgramMapperOptions;
import io.storedmapper.ParameterDirection;
import io.storedmapper.annotation.DbParameterName;
import io.storedmapper.annotation.DbParameterOrder;
import io.storedmapper.annotation.DbParameterProperty;
import io.storedmapper.annotation.DbProgramName;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * DBプログラム関連のヘルパーメソッド。
 *
 * <p>パッケージプライベート。リフレクションによるフィールド解析、SQL生成、
 * パラメータ構築を担当します。</p>
 */
public final class DbProgramHelper {

    private DbProgramHelper() {
    }

    /**
     * パラメータオブジェクトから{@link DbProgramName}アノテーションを取得します。
     *
     * @param param パラメータオブジェクト
     * @return DbProgramNameアノテーション（未設定の場合は{@code null}）
     */
    public static DbProgramName getDbProgramNameAnnotation(DbProgram param) {
        if (param == null) {
            return null;
        }
        return param.getClass().getAnnotation(DbProgramName.class);
    }

    /**
     * Dialectを使用して完全修飾名を取得します。
     *
     * @param annotation DbProgramNameアノテーション
     * @return 完全修飾名
     */
    public static String getFullName(DbProgramName annotation) {
        var schema = annotation.schema();
        if (schema == null || schema.isEmpty()) {
            schema = DbProgramMapperOptions.getDefaultSchema();
        }
        return DbProgramMapperOptions.getDialect().formatFullName(schema, annotation.value());
    }

    /**
     * テーブル値関数のクエリを生成します。
     *
     * @param annotation DbProgramNameアノテーション
     * @param param パラメータオブジェクト
     * @param orderByExpression ORDER BY句（nullの場合は省略）
     * @return SQL文
     */
    public static String createTableFunctionQuery(DbProgramName annotation, DbProgram param, String orderByExpression) {
        var fullName = getFullName(annotation);
        var placeholders = getParameterPlaceholders(param);
        return DbProgramMapperOptions.getDialect().createTableFunctionQuery(fullName, placeholders, orderByExpression);
    }

    /**
     * スカラー関数のクエリを生成します。
     *
     * @param annotation DbProgramNameアノテーション
     * @param param パラメータオブジェクト
     * @return SQL文
     */
    public static String createScalarFunctionQuery(DbProgramName annotation, DbProgram param) {
        var fullName = getFullName(annotation);
        var placeholders = getParameterPlaceholders(param);
        return DbProgramMapperOptions.getDialect().createScalarFunctionQuery(fullName, placeholders);
    }

    /**
     * ストアドプロシージャのCALL文を生成します。
     *
     * @param annotation DbProgramNameアノテーション
     * @param param パラメータオブジェクト
     * @return SQL文
     */
    public static String createStoredProcedureCall(DbProgramName annotation, DbProgram param) {
        var fullName = getFullName(annotation);
        var placeholders = getParameterPlaceholders(param);
        return DbProgramMapperOptions.getDialect().createStoredProcedureCall(fullName, placeholders);
    }

    /**
     * {@code @DbParameterOrder}でソートされたフィールドリストを取得します。
     * INPUTおよびINPUT_OUTPUT方向のフィールドのみを返します。
     *
     * @param param パラメータオブジェクト
     * @return ソートされたフィールドリスト
     */
    public static List<Field> getOrderedInputFields(DbProgram param) {
        var allFields = getAllFields(param);
        return allFields.stream()
                .filter(f -> {
                    var prop = f.getAnnotation(DbParameterProperty.class);
                    if (prop == null) {
                        return true; // デフォルトはINPUT
                    }
                    return prop.direction() != ParameterDirection.OUTPUT;
                })
                .sorted(Comparator.comparingInt(f -> getParameterOrder(f)))
                .toList();
    }

    /**
     * すべてのパラメータフィールドを{@code @DbParameterOrder}でソートして取得します。
     *
     * @param param パラメータオブジェクト
     * @return ソートされたフィールドリスト
     */
    public static List<Field> getAllOrderedFields(DbProgram param) {
        var allFields = getAllFields(param);
        return allFields.stream()
                .sorted(Comparator.comparingInt(f -> getParameterOrder(f)))
                .toList();
    }

    /**
     * SQLプレースホルダーリストを生成します。
     *
     * @param param パラメータオブジェクト
     * @return プレースホルダーのリスト（各要素は"?"）
     */
    public static List<String> getParameterPlaceholders(DbProgram param) {
        var fields = getOrderedInputFields(param);
        return fields.stream().map(f -> "?").toList();
    }

    /**
     * INPUTパラメータの値を配列として構築します。
     *
     * @param param パラメータオブジェクト
     * @return パラメータ値の配列
     */
    public static Object[] buildParameterArray(DbProgram param) {
        var fields = getOrderedInputFields(param);
        var values = new Object[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            var field = fields.get(i);
            field.setAccessible(true);
            try {
                values[i] = field.get(param);
            } catch (IllegalAccessException e) {
                values[i] = null;
            }
        }
        return values;
    }

    /**
     * OUTPUTパラメータを持つフィールドリストを取得します。
     *
     * @param param パラメータオブジェクト
     * @return OUTPUT/INPUT_OUTPUTフィールドのリスト
     */
    public static List<Field> getOutputFields(DbProgram param) {
        var allFields = getAllFields(param);
        return allFields.stream()
                .filter(f -> {
                    var prop = f.getAnnotation(DbParameterProperty.class);
                    return prop != null && (prop.direction() == ParameterDirection.OUTPUT
                            || prop.direction() == ParameterDirection.INPUT_OUTPUT);
                })
                .toList();
    }

    /**
     * フィールドのSQLパラメータ名を取得します。
     * {@code @DbParameterName}が設定されている場合はその値を、なければフィールド名を返します。
     *
     * @param field フィールド
     * @return SQLパラメータ名
     */
    public static String getParameterName(Field field) {
        var nameAnnotation = field.getAnnotation(DbParameterName.class);
        if (nameAnnotation != null) {
            return nameAnnotation.value();
        }
        return field.getName();
    }

    /**
     * OUTPUTパラメータの値をオブジェクトに設定します。
     *
     * @param param パラメータオブジェクト
     * @param field 設定対象のフィールド
     * @param value 設定する値
     */
    public static void setOutputParameterValue(DbProgram param, Field field, Object value) {
        field.setAccessible(true);
        try {
            field.set(param, value);
        } catch (IllegalAccessException e) {
            // フィールドへのアクセスに失敗した場合はスキップ
        }
    }

    private static List<Field> getAllFields(DbProgram param) {
        var fields = new ArrayList<Field>();
        Class<?> clazz = param.getClass();
        while (clazz != null && clazz != Object.class) {
            for (var field : clazz.getDeclaredFields()) {
                // DbParameterOrderまたはDbParameterPropertyのアノテーションを持つフィールドのみ
                if (field.isAnnotationPresent(DbParameterOrder.class)
                        || field.isAnnotationPresent(DbParameterProperty.class)) {
                    fields.add(field);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private static int getParameterOrder(Field field) {
        var orderAnnotation = field.getAnnotation(DbParameterOrder.class);
        if (orderAnnotation != null) {
            return orderAnnotation.value();
        }
        return Integer.MAX_VALUE;
    }
}
