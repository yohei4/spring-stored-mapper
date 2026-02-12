package io.storedmapper;

import io.storedmapper.annotation.DbParameterProperty;

import java.sql.Types;

/**
 * エラー情報付きDBプログラム基底クラス。
 *
 * <p>{@code sqlErrorCd}と{@code progressMessage}のOUTPUTパラメータを持つ
 * ストアドプロシージャ用の基底クラスです。</p>
 *
 * <pre>{@code
 * @DbProgramName("sp_update_user")
 * public class UpdateUserParam extends DbProgramWithErrorBase {
 *     @DbParameterOrder(1) private UUID userId;
 *     @DbParameterOrder(2) private String name;
 *
 *     public UpdateUserParam(UUID userId, String name) {
 *         this.userId = userId;
 *         this.name = name;
 *     }
 * }
 *
 * // 実行後
 * if (param.hasSqlError()) {
 *     log.error("Error: {}", param.getProgressMessage());
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
public abstract class DbProgramWithErrorBase extends DbProgramBase {

    /** SQLエラーコード（OUTPUT）。0は正常、それ以外はエラー */
    @DbParameterProperty(sqlType = Types.INTEGER, direction = ParameterDirection.OUTPUT)
    private Integer sqlErrorCd;

    /** 進捗・エラーメッセージ（OUTPUT） */
    @DbParameterProperty(sqlType = Types.VARCHAR, direction = ParameterDirection.OUTPUT, size = 4000)
    private String progressMessage;

    protected DbProgramWithErrorBase() {
    }

    protected DbProgramWithErrorBase(Object source) {
        super(source);
    }

    /**
     * SQLエラーが発生したかどうかを返します。
     *
     * @return エラーが発生した場合は{@code true}
     */
    public boolean hasSqlError() {
        return sqlErrorCd != null && sqlErrorCd != 0;
    }

    public Integer getSqlErrorCd() {
        return sqlErrorCd;
    }

    public void setSqlErrorCd(Integer sqlErrorCd) {
        this.sqlErrorCd = sqlErrorCd;
    }

    public String getProgressMessage() {
        return progressMessage;
    }

    public void setProgressMessage(String progressMessage) {
        this.progressMessage = progressMessage;
    }
}
