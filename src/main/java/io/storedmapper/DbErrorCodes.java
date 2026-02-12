package io.storedmapper;

/**
 * データベースエラーコード設定。
 *
 * <p>ストアドプロシージャのRETURN値によるエラー判定に使用します。
 * {@code null}を設定した場合、そのエラー判定は無効になります。</p>
 *
 * <pre>{@code
 * DbProgramMapperOptions.configure(config -> {
 *     var errorCodes = new DbErrorCodes();
 *     errorCodes.setNotFound(1);
 *     errorCodes.setDuplicate(2);
 *     errorCodes.setOptimisticLock(3);
 *     config.setErrorCodes(errorCodes);
 * });
 * }</pre>
 *
 * @since 1.0.0
 */
public class DbErrorCodes {

    /** 成功を示すコード（デフォルト: 0） */
    private int success = 0;

    /** 排他エラー（レコードロック中） */
    private Integer exclusiveLock;

    /** 楽観的ロック失敗（バージョン不一致） */
    private Integer optimisticLock;

    /** 重複エラー（一意制約違反） */
    private Integer duplicate;

    /** 存在しないエラー（レコードが見つからない） */
    private Integer notFound;

    /** 参照整合性エラー（外部キー制約違反） */
    private Integer foreignKeyViolation;

    /** 権限エラー（操作権限なし） */
    private Integer permissionDenied;

    /** バリデーションエラー（業務ルール違反） */
    private Integer validationError;

    /** デッドロック検出 */
    private Integer deadlock;

    /** タイムアウト */
    private Integer timeout;

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public Integer getExclusiveLock() {
        return exclusiveLock;
    }

    public void setExclusiveLock(Integer exclusiveLock) {
        this.exclusiveLock = exclusiveLock;
    }

    public Integer getOptimisticLock() {
        return optimisticLock;
    }

    public void setOptimisticLock(Integer optimisticLock) {
        this.optimisticLock = optimisticLock;
    }

    public Integer getDuplicate() {
        return duplicate;
    }

    public void setDuplicate(Integer duplicate) {
        this.duplicate = duplicate;
    }

    public Integer getNotFound() {
        return notFound;
    }

    public void setNotFound(Integer notFound) {
        this.notFound = notFound;
    }

    public Integer getForeignKeyViolation() {
        return foreignKeyViolation;
    }

    public void setForeignKeyViolation(Integer foreignKeyViolation) {
        this.foreignKeyViolation = foreignKeyViolation;
    }

    public Integer getPermissionDenied() {
        return permissionDenied;
    }

    public void setPermissionDenied(Integer permissionDenied) {
        this.permissionDenied = permissionDenied;
    }

    public Integer getValidationError() {
        return validationError;
    }

    public void setValidationError(Integer validationError) {
        this.validationError = validationError;
    }

    public Integer getDeadlock() {
        return deadlock;
    }

    public void setDeadlock(Integer deadlock) {
        this.deadlock = deadlock;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * 設定をコピーします。
     *
     * @return コピーされた設定
     */
    DbErrorCodes copy() {
        var clone = new DbErrorCodes();
        clone.success = this.success;
        clone.exclusiveLock = this.exclusiveLock;
        clone.optimisticLock = this.optimisticLock;
        clone.duplicate = this.duplicate;
        clone.notFound = this.notFound;
        clone.foreignKeyViolation = this.foreignKeyViolation;
        clone.permissionDenied = this.permissionDenied;
        clone.validationError = this.validationError;
        clone.deadlock = this.deadlock;
        clone.timeout = this.timeout;
        return clone;
    }
}
