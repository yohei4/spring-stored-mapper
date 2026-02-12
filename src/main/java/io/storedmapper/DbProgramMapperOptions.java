package io.storedmapper;

import io.storedmapper.dialect.DbDialect;
import io.storedmapper.dialect.SqlServerDialect;

import java.util.function.Consumer;

/**
 * DbProgramMapperのグローバル設定。
 *
 * <p>アプリケーション起動時に一度設定し、ライブラリ全体で共有します。</p>
 *
 * <pre>{@code
 * DbProgramMapperOptions.configure(config -> {
 *     config.setDialect(new PostgreSqlDialect());
 *     config.setDefaultSchema("public");
 * });
 * }</pre>
 *
 * @since 1.0.0
 */
public final class DbProgramMapperOptions {

    private static DbDialect dialect = new SqlServerDialect();
    private static String defaultSchema = "dbo";
    private static DbErrorCodes errorCodes = new DbErrorCodes();

    private DbProgramMapperOptions() {
    }

    /**
     * 使用するデータベース方言を返します。
     *
     * @return データベース方言
     */
    public static DbDialect getDialect() {
        return dialect;
    }

    /**
     * デフォルトスキーマ名を返します。
     *
     * @return デフォルトスキーマ名
     */
    public static String getDefaultSchema() {
        return defaultSchema;
    }

    /**
     * エラーコード設定を返します。
     *
     * @return エラーコード設定
     */
    public static DbErrorCodes getErrorCodes() {
        return errorCodes;
    }

    /**
     * 設定を構成します。
     *
     * @param configurer 設定アクション
     */
    public static void configure(Consumer<DbProgramMapperConfiguration> configurer) {
        var config = new DbProgramMapperConfiguration();
        configurer.accept(config);

        if (config.getDialect() != null) {
            dialect = config.getDialect();
        }
        if (config.getDefaultSchema() != null) {
            defaultSchema = config.getDefaultSchema();
        }
        if (config.getErrorCodes() != null) {
            errorCodes = config.getErrorCodes();
        }
    }

    /**
     * 設定をデフォルトにリセットします。
     */
    public static void reset() {
        dialect = new SqlServerDialect();
        defaultSchema = "dbo";
        errorCodes = new DbErrorCodes();
    }
}
