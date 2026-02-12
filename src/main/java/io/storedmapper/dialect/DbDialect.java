package io.storedmapper.dialect;

import java.util.List;

/**
 * データベース方言インターフェース。
 *
 * <p>データベース固有のSQL構文（識別子のクォート、関数呼び出し、プロシージャ呼び出し）を
 * 抽象化します。</p>
 *
 * @since 1.0.0
 */
public interface DbDialect {

    /**
     * スキーマとオブジェクト名から完全修飾名を生成します。
     *
     * @param schema スキーマ名
     * @param name オブジェクト名
     * @return 完全修飾名
     */
    String formatFullName(String schema, String name);

    /**
     * テーブル値関数のSELECTクエリを生成します。
     *
     * @param fullName 完全修飾名
     * @param parameters パラメータプレースホルダのリスト
     * @param orderByExpression ORDER BY句（nullまたは空の場合は省略）
     * @return SQL文
     */
    String createTableFunctionQuery(String fullName, List<String> parameters, String orderByExpression);

    /**
     * スカラー値関数のSELECTクエリを生成します。
     *
     * @param fullName 完全修飾名
     * @param parameters パラメータプレースホルダのリスト
     * @return SQL文
     */
    String createScalarFunctionQuery(String fullName, List<String> parameters);

    /**
     * ストアドプロシージャのCALL文を生成します。
     *
     * @param fullName 完全修飾名
     * @param parameters パラメータプレースホルダのリスト
     * @return SQL文
     */
    String createStoredProcedureCall(String fullName, List<String> parameters);
}
