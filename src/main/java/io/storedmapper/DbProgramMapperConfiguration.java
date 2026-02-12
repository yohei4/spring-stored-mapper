package io.storedmapper;

import io.storedmapper.dialect.DbDialect;

/**
 * DbProgramMapper設定クラス。
 *
 * <p>{@link DbProgramMapperOptions#configure(java.util.function.Consumer)} で使用します。</p>
 *
 * @since 1.0.0
 */
public class DbProgramMapperConfiguration {

    private DbDialect dialect;
    private String defaultSchema;
    private DbErrorCodes errorCodes;

    public DbDialect getDialect() {
        return dialect;
    }

    public void setDialect(DbDialect dialect) {
        this.dialect = dialect;
    }

    public String getDefaultSchema() {
        return defaultSchema;
    }

    public void setDefaultSchema(String defaultSchema) {
        this.defaultSchema = defaultSchema;
    }

    public DbErrorCodes getErrorCodes() {
        return errorCodes;
    }

    public void setErrorCodes(DbErrorCodes errorCodes) {
        this.errorCodes = errorCodes;
    }
}
