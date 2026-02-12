package io.storedmapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DBプログラム（ストアドプロシージャ/関数）の名前とスキーマを指定するアノテーション。
 *
 * <p>スキーマを省略した場合は {@link io.storedmapper.DbProgramMapperOptions#getDefaultSchema()}
 * が使用されます。</p>
 *
 * <pre>{@code
 * // デフォルトスキーマを使用
 * @DbProgramName("sp_get_users")
 *
 * // スキーマを明示的に指定
 * @DbProgramName(value = "sp_get_users", schema = "sales")
 * }</pre>
 *
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DbProgramName {

    /** DBプログラム名 */
    String value();

    /** スキーマ名（空文字の場合はデフォルトスキーマを使用） */
    String schema() default "";
}
