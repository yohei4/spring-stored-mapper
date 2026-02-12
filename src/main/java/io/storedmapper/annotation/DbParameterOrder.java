package io.storedmapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * パラメータの順序を指定するアノテーション。
 *
 * <p>テーブル値関数やスカラー値関数では、パラメータの順序がSQL文のプレースホルダと対応するため、
 * このアノテーションで明示的に順序を指定してください。</p>
 *
 * <pre>{@code
 * @DbProgramName("sp_get_tasks")
 * public class GetTasksParam extends DbProgramBase {
 *     @DbParameterOrder(1) private UUID userId;
 *     @DbParameterOrder(2) private Integer limit;
 *     @DbParameterOrder(3) private Integer offset;
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DbParameterOrder {

    /** パラメータの順序（1始まり） */
    int value();
}
