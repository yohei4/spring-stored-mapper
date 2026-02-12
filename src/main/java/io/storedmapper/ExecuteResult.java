package io.storedmapper;

/**
 * ストアドプロシージャ実行結果。
 *
 * <p>RETURN値を返さないストアドプロシージャにも対応しており、
 * {@code returnCode}が{@code null}の場合は{@link #isSuccess()}が{@code true}を返します。</p>
 *
 * <pre>{@code
 * ExecuteResult result = executor.execute(param);
 *
 * if (result.hasError()) {
 *     if (result.isNotFoundError()) {
 *         throw new NotFoundException("Record not found");
 *     }
 *     if (result.isDuplicateError()) {
 *         throw new ConflictException("Duplicate entry");
 *     }
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
public class ExecuteResult {

    /** 影響を受けた行数 */
    private int affectedRows;

    /** ストアドプロシージャのRETURN値（nullの場合はRETURN値なし） */
    private Integer returnCode;

    public ExecuteResult() {
    }

    public ExecuteResult(int affectedRows, Integer returnCode) {
        this.affectedRows = affectedRows;
        this.returnCode = returnCode;
    }

    public int getAffectedRows() {
        return affectedRows;
    }

    public void setAffectedRows(int affectedRows) {
        this.affectedRows = affectedRows;
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(Integer returnCode) {
        this.returnCode = returnCode;
    }

    // --- エラー判定メソッド ---

    /**
     * 成功かどうかを返します。
     * RETURN値がない場合（{@code null}）も成功とみなします。
     *
     * @return 成功の場合は{@code true}
     */
    public boolean isSuccess() {
        if (returnCode == null) {
            return true;
        }
        return returnCode == DbProgramMapperOptions.getErrorCodes().getSuccess();
    }

    /**
     * 何らかのエラーが発生したかどうかを返します。
     *
     * @return エラーの場合は{@code true}
     */
    public boolean hasError() {
        return !isSuccess();
    }

    /**
     * 排他エラー（レコードロック中）かどうかを返します。
     *
     * @return 排他エラーの場合は{@code true}
     */
    public boolean isExclusiveLockError() {
        return matchesErrorCode(DbProgramMapperOptions.getErrorCodes().getExclusiveLock());
    }

    /**
     * 楽観的ロック失敗かどうかを返します。
     *
     * @return 楽観的ロックエラーの場合は{@code true}
     */
    public boolean isOptimisticLockError() {
        return matchesErrorCode(DbProgramMapperOptions.getErrorCodes().getOptimisticLock());
    }

    /**
     * 重複エラー（一意制約違反）かどうかを返します。
     *
     * @return 重複エラーの場合は{@code true}
     */
    public boolean isDuplicateError() {
        return matchesErrorCode(DbProgramMapperOptions.getErrorCodes().getDuplicate());
    }

    /**
     * 存在しないエラー（レコードが見つからない）かどうかを返します。
     *
     * @return NotFoundエラーの場合は{@code true}
     */
    public boolean isNotFoundError() {
        return matchesErrorCode(DbProgramMapperOptions.getErrorCodes().getNotFound());
    }

    /**
     * 参照整合性エラー（外部キー制約違反）かどうかを返します。
     *
     * @return 外部キー制約違反の場合は{@code true}
     */
    public boolean isForeignKeyViolationError() {
        return matchesErrorCode(DbProgramMapperOptions.getErrorCodes().getForeignKeyViolation());
    }

    /**
     * 権限エラーかどうかを返します。
     *
     * @return 権限エラーの場合は{@code true}
     */
    public boolean isPermissionDeniedError() {
        return matchesErrorCode(DbProgramMapperOptions.getErrorCodes().getPermissionDenied());
    }

    /**
     * バリデーションエラー（業務ルール違反）かどうかを返します。
     *
     * @return バリデーションエラーの場合は{@code true}
     */
    public boolean isValidationError() {
        return matchesErrorCode(DbProgramMapperOptions.getErrorCodes().getValidationError());
    }

    /**
     * デッドロックかどうかを返します。
     *
     * @return デッドロックの場合は{@code true}
     */
    public boolean isDeadlockError() {
        return matchesErrorCode(DbProgramMapperOptions.getErrorCodes().getDeadlock());
    }

    /**
     * タイムアウトかどうかを返します。
     *
     * @return タイムアウトの場合は{@code true}
     */
    public boolean isTimeoutError() {
        return matchesErrorCode(DbProgramMapperOptions.getErrorCodes().getTimeout());
    }

    private boolean matchesErrorCode(Integer errorCode) {
        return errorCode != null && returnCode != null && returnCode.equals(errorCode);
    }
}
