package io.storedmapper.annotation;

import io.storedmapper.ParameterDirection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * パラメータの詳細設定（SQLタイプ、方向、サイズ）を指定するアノテーション。
 *
 * <p>主にOUTPUTパラメータやINPUT/OUTPUTパラメータの定義に使用します。</p>
 *
 * <pre>{@code
 * // OUTPUTパラメータ
 * @DbParameterProperty(sqlType = Types.INTEGER, direction = ParameterDirection.OUTPUT)
 * private Integer errorCode;
 *
 * // サイズ指定付きOUTPUTパラメータ
 * @DbParameterProperty(sqlType = Types.VARCHAR, direction = ParameterDirection.OUTPUT, size = 4000)
 * private String message;
 * }</pre>
 *
 * @since 1.0.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DbParameterProperty {

    /** SQLタイプ（{@link java.sql.Types} の定数）。未指定の場合は自動判定 */
    int sqlType() default Integer.MIN_VALUE;

    /** パラメータの方向（デフォルト: INPUT） */
    ParameterDirection direction() default ParameterDirection.INPUT;

    /** パラメータサイズ（-1は未指定） */
    int size() default -1;
}
