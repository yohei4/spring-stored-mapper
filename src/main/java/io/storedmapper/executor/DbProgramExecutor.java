package io.storedmapper.executor;

import io.storedmapper.DbProgram;
import io.storedmapper.ExecuteResult;
import io.storedmapper.ParameterDirection;
import io.storedmapper.annotation.DbParameterProperty;
import io.storedmapper.annotation.DbProgramName;
import io.storedmapper.internal.DbProgramHelper;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlInOutParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DBプログラム統一実行コンポーネント。
 *
 * <p>ストアドプロシージャ、テーブル値関数、スカラー値関数の実行を
 * 統一的なAPIで提供します。</p>
 *
 * <pre>{@code
 * @Autowired
 * private DbProgramExecutor executor;
 *
 * // ストアドプロシージャ実行
 * ExecuteResult result = executor.execute(param);
 *
 * // テーブル値関数（リスト取得）
 * List<TaskDto> tasks = executor.query(param, TaskDto.class);
 *
 * // スカラー値関数
 * Integer count = executor.executeScalar(param, Integer.class);
 * }</pre>
 *
 * @since 1.0.0
 */
@Component
public class DbProgramExecutor {

    private final JdbcTemplate jdbcTemplate;

    public DbProgramExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // --- ストアドプロシージャ実行 ---

    /**
     * ストアドプロシージャを実行します。
     *
     * @param param パラメータオブジェクト
     * @return 実行結果
     */
    public ExecuteResult execute(DbProgram param) {
        var annotation = requireDbProgramName(param);
        var outputFields = DbProgramHelper.getOutputFields(param);

        if (outputFields.isEmpty()) {
            // OUTPUT パラメータなし: シンプルな実行
            var sql = DbProgramHelper.createStoredProcedureCall(annotation, param);
            var args = DbProgramHelper.buildParameterArray(param);
            var affectedRows = jdbcTemplate.update(sql, args);
            return new ExecuteResult(affectedRows, null);
        }

        // OUTPUT パラメータあり: SimpleJdbcCall を使用
        return executeWithOutputParameters(param, annotation, outputFields);
    }

    private ExecuteResult executeWithOutputParameters(DbProgram param, DbProgramName annotation, List<Field> outputFields) {
        var schema = annotation.schema();
        if (schema == null || schema.isEmpty()) {
            schema = io.storedmapper.DbProgramMapperOptions.getDefaultSchema();
        }

        var call = new SimpleJdbcCall(jdbcTemplate)
                .withSchemaName(schema)
                .withProcedureName(annotation.value());

        // パラメータ宣言を構築
        var sqlParams = buildSqlParameters(param);
        call.declareParameters(sqlParams.toArray(new SqlParameter[0]));

        // 入力パラメータ値を構築
        var inputValues = buildInputParameterMap(param);

        // 実行
        var result = call.execute(inputValues);

        // OUTPUT パラメータの値をオブジェクトに設定
        for (var field : outputFields) {
            var paramName = DbProgramHelper.getParameterName(field);
            var value = result.get(paramName);
            DbProgramHelper.setOutputParameterValue(param, field, value);
        }

        // RETURN_VALUE の取得（あれば）
        var returnValue = result.get("return");
        Integer returnCode = null;
        if (returnValue instanceof Number number) {
            returnCode = number.intValue();
        }

        return new ExecuteResult(0, returnCode);
    }

    // --- テーブル値関数（リスト取得） ---

    /**
     * テーブル値関数を実行し、結果をリストとして取得します。
     *
     * @param <T> 結果の型
     * @param param パラメータオブジェクト
     * @param resultType 結果クラス（BeanPropertyRowMapperで自動マッピング）
     * @return 結果リスト
     */
    public <T> List<T> query(DbProgram param, Class<T> resultType) {
        return query(param, resultType, null);
    }

    /**
     * テーブル値関数を実行し、ORDER BY付きで結果をリストとして取得します。
     *
     * @param <T> 結果の型
     * @param param パラメータオブジェクト
     * @param resultType 結果クラス
     * @param orderBy ORDER BY句
     * @return 結果リスト
     */
    public <T> List<T> query(DbProgram param, Class<T> resultType, String orderBy) {
        return query(param, new BeanPropertyRowMapper<>(resultType), orderBy);
    }

    /**
     * テーブル値関数を実行し、カスタムRowMapperで結果をリストとして取得します。
     *
     * @param <T> 結果の型
     * @param param パラメータオブジェクト
     * @param rowMapper カスタムRowMapper
     * @return 結果リスト
     */
    public <T> List<T> query(DbProgram param, RowMapper<T> rowMapper) {
        return query(param, rowMapper, null);
    }

    /**
     * テーブル値関数を実行し、カスタムRowMapperとORDER BY付きで結果をリストとして取得します。
     *
     * @param <T> 結果の型
     * @param param パラメータオブジェクト
     * @param rowMapper カスタムRowMapper
     * @param orderBy ORDER BY句
     * @return 結果リスト
     */
    public <T> List<T> query(DbProgram param, RowMapper<T> rowMapper, String orderBy) {
        var annotation = requireDbProgramName(param);
        var sql = DbProgramHelper.createTableFunctionQuery(annotation, param, orderBy);
        var args = DbProgramHelper.buildParameterArray(param);
        return jdbcTemplate.query(sql, rowMapper, args);
    }

    // --- テーブル値関数（先頭1件） ---

    /**
     * テーブル値関数を実行し、先頭1件を取得します。
     * 結果がない場合は{@code null}を返します。
     *
     * @param <T> 結果の型
     * @param param パラメータオブジェクト
     * @param resultType 結果クラス
     * @return 先頭1件（結果がない場合は{@code null}）
     */
    public <T> T queryFirstOrDefault(DbProgram param, Class<T> resultType) {
        return queryFirstOrDefault(param, new BeanPropertyRowMapper<>(resultType));
    }

    /**
     * テーブル値関数を実行し、カスタムRowMapperで先頭1件を取得します。
     *
     * @param <T> 結果の型
     * @param param パラメータオブジェクト
     * @param rowMapper カスタムRowMapper
     * @return 先頭1件（結果がない場合は{@code null}）
     */
    public <T> T queryFirstOrDefault(DbProgram param, RowMapper<T> rowMapper) {
        var annotation = requireDbProgramName(param);
        var sql = DbProgramHelper.createTableFunctionQuery(annotation, param, null);
        var args = DbProgramHelper.buildParameterArray(param);
        var results = jdbcTemplate.query(sql, rowMapper, args);
        return results.isEmpty() ? null : results.getFirst();
    }

    // --- スカラー値関数 ---

    /**
     * スカラー値関数を実行し、結果を取得します。
     *
     * @param <T> 結果の型
     * @param param パラメータオブジェクト
     * @param resultType 結果クラス
     * @return スカラー値
     */
    public <T> T executeScalar(DbProgram param, Class<T> resultType) {
        var annotation = requireDbProgramName(param);
        var sql = DbProgramHelper.createScalarFunctionQuery(annotation, param);
        var args = DbProgramHelper.buildParameterArray(param);
        return jdbcTemplate.queryForObject(sql, resultType, args);
    }

    // --- private methods ---

    private DbProgramName requireDbProgramName(DbProgram param) {
        var annotation = DbProgramHelper.getDbProgramNameAnnotation(param);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    "DbProgramName annotation is not set on " + param.getClass().getName()
                            + ". Please add @DbProgramName to the class.");
        }
        return annotation;
    }

    private List<SqlParameter> buildSqlParameters(DbProgram param) {
        var sqlParams = new ArrayList<SqlParameter>();
        var fields = DbProgramHelper.getAllOrderedFields(param);

        for (var field : fields) {
            var paramName = DbProgramHelper.getParameterName(field);
            var prop = field.getAnnotation(DbParameterProperty.class);

            int sqlType = determineSqlType(field, prop);
            var direction = prop != null ? prop.direction() : io.storedmapper.ParameterDirection.INPUT;

            switch (direction) {
                case OUTPUT -> sqlParams.add(new SqlOutParameter(paramName, sqlType));
                case INPUT_OUTPUT -> sqlParams.add(new SqlInOutParameter(paramName, sqlType));
                default -> sqlParams.add(new SqlParameter(paramName, sqlType));
            }
        }

        return sqlParams;
    }

    private Map<String, Object> buildInputParameterMap(DbProgram param) {
        var map = new java.util.HashMap<String, Object>();
        var fields = DbProgramHelper.getAllOrderedFields(param);

        for (var field : fields) {
            var prop = field.getAnnotation(DbParameterProperty.class);
            var direction = prop != null ? prop.direction() : io.storedmapper.ParameterDirection.INPUT;

            if (direction != ParameterDirection.OUTPUT) {
                var paramName = DbProgramHelper.getParameterName(field);
                field.setAccessible(true);
                try {
                    map.put(paramName, field.get(param));
                } catch (IllegalAccessException e) {
                    // skip
                }
            }
        }

        return map;
    }

    private int determineSqlType(Field field, DbParameterProperty prop) {
        if (prop != null && prop.sqlType() != Integer.MIN_VALUE) {
            return prop.sqlType();
        }
        // フィールド型からの推定
        var type = field.getType();
        if (type == String.class) return Types.VARCHAR;
        if (type == Integer.class || type == int.class) return Types.INTEGER;
        if (type == Long.class || type == long.class) return Types.BIGINT;
        if (type == Boolean.class || type == boolean.class) return Types.BOOLEAN;
        if (type == java.util.UUID.class) return Types.OTHER;
        if (type == java.sql.Timestamp.class || type == java.time.LocalDateTime.class) return Types.TIMESTAMP;
        if (type == java.sql.Date.class || type == java.time.LocalDate.class) return Types.DATE;
        return Types.OTHER;
    }
}
