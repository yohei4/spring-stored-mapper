package io.storedmapper;

/**
 * ストアドプロシージャのパラメータ方向を示す列挙型。
 *
 * @since 1.0.0
 */
public enum ParameterDirection {

    /** 入力パラメータ（デフォルト） */
    INPUT,

    /** 出力パラメータ */
    OUTPUT,

    /** 入出力パラメータ */
    INPUT_OUTPUT
}
