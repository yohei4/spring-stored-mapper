package io.storedmapper;

import java.lang.reflect.Field;

/**
 * DBプログラムパラメータの基底クラス。
 *
 * <p>すべてのDBプログラムパラメータクラスはこのクラスを継承します。
 * ソースオブジェクトから同名・同型フィールドの値を自動コピーする機能を提供します。</p>
 *
 * <pre>{@code
 * @DbProgramName("sp_get_users")
 * public class GetUsersParam extends DbProgramBase {
 *     @DbParameterOrder(1)
 *     private Integer departmentId;
 *
 *     public GetUsersParam(Integer departmentId) {
 *         this.departmentId = departmentId;
 *     }
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
public abstract class DbProgramBase implements DbProgram {

    /**
     * デフォルトコンストラクタ。
     */
    protected DbProgramBase() {
    }

    /**
     * ソースオブジェクトからフィールド値をコピーしてインスタンスを生成します。
     *
     * <p>同名・同型のフィールドの値がコピーされます。
     * 空文字列は{@code null}に変換されます。</p>
     *
     * @param source コピー元オブジェクト
     */
    protected DbProgramBase(Object source) {
        if (source == null) {
            return;
        }
        copyFieldsFrom(source);
    }

    private void copyFieldsFrom(Object source) {
        Field[] targetFields = this.getClass().getDeclaredFields();
        for (Field targetField : targetFields) {
            try {
                Field sourceField = findField(source.getClass(), targetField.getName());
                if (sourceField != null && sourceField.getType().equals(targetField.getType())) {
                    sourceField.setAccessible(true);
                    targetField.setAccessible(true);
                    Object value = sourceField.get(source);
                    if (value instanceof String s && s.isEmpty()) {
                        value = null;
                    }
                    targetField.set(this, value);
                }
            } catch (IllegalAccessException e) {
                // フィールドへのアクセスに失敗した場合はスキップ
            }
        }
    }

    private static Field findField(Class<?> clazz, String name) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
