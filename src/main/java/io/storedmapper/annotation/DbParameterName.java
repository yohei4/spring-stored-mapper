package io.storedmapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * フィールド名と異なるSQLパラメータ名を使用する場合に指定するアノテーション。
 *
 * <p>省略した場合はフィールド名がそのままパラメータ名として使用されます。</p>
 *
 * <pre>{@code
 * @DbParameterName("p_user_id")
 * private UUID userId;
 * }</pre>
 *
 * @since 1.0.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DbParameterName {

    /** SQLパラメータ名 */
    String value();
}
