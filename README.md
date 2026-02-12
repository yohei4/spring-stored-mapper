# spring-stored-mapper

Spring JDBC向けのアノテーションベース ストアドプロシージャ/関数マッパーライブラリ。

## 概要

ストアドプロシージャ、テーブル値関数、スカラー値関数の呼び出しを、アノテーションとPOJOベースのパラメータクラスで統一的に扱えるようにするライブラリです。

## 動作要件

- Java 21+
- Spring Framework 6.2+
- Spring JDBC

## セットアップ

### Maven

```xml
<dependency>
    <groupId>io.storedmapper</groupId>
    <artifactId>spring-stored-mapper</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 初期設定

アプリケーション起動時にグローバル設定を行います。デフォルトはSQL Server + `dbo`スキーマです。

```java
// PostgreSQLの場合
DbProgramMapperOptions.configure(config -> {
    config.setDialect(new PostgreSqlDialect());
    config.setDefaultSchema("public");
});

// MySQLの場合
DbProgramMapperOptions.configure(config -> {
    config.setDialect(new MySqlDialect());
    config.setDefaultSchema("mydb");
});
```

## 基本的な使い方

### 1. パラメータクラスの定義

```java
@DbProgramName("sp_get_tasks")
public class GetTasksParam extends DbProgramBase {
    @DbParameterOrder(1) private UUID userId;
    @DbParameterOrder(2) private Integer limit;
    @DbParameterOrder(3) private Integer offset;

    public GetTasksParam(UUID userId, int limit, int offset) {
        this.userId = userId;
        this.limit = limit;
        this.offset = offset;
    }
}
```

### 2. DbProgramExecutorで実行

```java
@Autowired
private DbProgramExecutor executor;
```

#### ストアドプロシージャ実行

```java
ExecuteResult result = executor.execute(param);

if (result.hasError()) {
    // エラー処理
}
```

#### テーブル値関数（リスト取得）

```java
List<TaskDto> tasks = executor.query(param, TaskDto.class);

// ORDER BY付き
List<TaskDto> tasks = executor.query(param, TaskDto.class, "created_at DESC");

// 先頭1件取得
TaskDto task = executor.queryFirstOrDefault(param, TaskDto.class);
```

#### スカラー値関数

```java
Integer count = executor.executeScalar(param, Integer.class);
```

## アノテーション一覧

| アノテーション | 対象 | 説明 |
|---|---|---|
| `@DbProgramName` | クラス | ストアドプロシージャ/関数名とスキーマを指定 |
| `@DbParameterOrder` | フィールド | パラメータの順序を指定（1始まり） |
| `@DbParameterName` | フィールド | フィールド名と異なるSQLパラメータ名を指定 |
| `@DbParameterProperty` | フィールド | SQLタイプ、方向（INPUT/OUTPUT/INPUT_OUTPUT）、サイズを指定 |

## OUTPUTパラメータ

`DbProgramWithErrorBase`を継承すると、`sqlErrorCd`と`progressMessage`のOUTPUTパラメータが自動的に追加されます。

```java
@DbProgramName("sp_update_user")
public class UpdateUserParam extends DbProgramWithErrorBase {
    @DbParameterOrder(1) private UUID userId;
    @DbParameterOrder(2) private String name;

    public UpdateUserParam(UUID userId, String name) {
        this.userId = userId;
        this.name = name;
    }
}

// 実行後
ExecuteResult result = executor.execute(param);
if (param.hasSqlError()) {
    log.error("Error: {}", param.getProgressMessage());
}
```

カスタムOUTPUTパラメータも定義できます:

```java
@DbParameterProperty(sqlType = Types.INTEGER, direction = ParameterDirection.OUTPUT)
private Integer errorCode;

@DbParameterProperty(sqlType = Types.VARCHAR, direction = ParameterDirection.OUTPUT, size = 4000)
private String message;
```

## エラーコード設定

ストアドプロシージャのRETURN値によるエラー判定をカスタマイズできます。

```java
DbProgramMapperOptions.configure(config -> {
    var errorCodes = new DbErrorCodes();
    errorCodes.setNotFound(1);
    errorCodes.setDuplicate(2);
    errorCodes.setOptimisticLock(3);
    errorCodes.setExclusiveLock(4);
    errorCodes.setForeignKeyViolation(5);
    errorCodes.setPermissionDenied(6);
    errorCodes.setValidationError(7);
    errorCodes.setDeadlock(8);
    errorCodes.setTimeout(9);
    config.setErrorCodes(errorCodes);
});

// 判定
ExecuteResult result = executor.execute(param);
if (result.isNotFoundError()) { /* ... */ }
if (result.isDuplicateError()) { /* ... */ }
if (result.isOptimisticLockError()) { /* ... */ }
```

## ソースオブジェクトからの自動コピー

`DbProgramBase`のコンストラクタにオブジェクトを渡すと、同名・同型のフィールド値が自動コピーされます。空文字列は`null`に変換されます。

```java
@DbProgramName("sp_update_user")
public class UpdateUserParam extends DbProgramBase {
    @DbParameterOrder(1) private UUID userId;
    @DbParameterOrder(2) private String name;

    public UpdateUserParam(Object source) {
        super(source); // sourceから同名フィールドをコピー
    }
}
```

## バリデーション

起動時にパラメータクラスの定義を検証できます。

```java
// エラー時に例外をスロー
DbProgramValidator.validate(GetTasksParam.class, UpdateUserParam.class);

// 検証結果を取得して個別処理
var result = DbProgramValidator.validateAndGetResult(GetTasksParam.class);
if (!result.isValid()) {
    result.getErrors().forEach(e -> log.warn(e.getMessage()));
}
```

検証項目:
- `@DbProgramName`アノテーションの有無
- プログラム名が空でないこと
- プログラム名・スキーマ名にSQLインジェクション文字が含まれていないこと
- `@DbParameterOrder`の順序値が重複していないこと

## 対応データベース

| データベース | Dialectクラス | 識別子形式 | プロシージャ呼び出し |
|---|---|---|---|
| SQL Server | `SqlServerDialect` | `[schema].[name]` | `{call ...}` |
| PostgreSQL | `PostgreSqlDialect` | `"schema"."name"` | `CALL ...` |
| MySQL | `MySqlDialect` | `` `schema`.`name` `` | `CALL ...` |

## プロジェクト構成

```
src/main/java/io/storedmapper/
├── DbProgram.java                  # マーカーインターフェース
├── DbProgramBase.java              # パラメータ基底クラス
├── DbProgramWithErrorBase.java     # エラー情報付き基底クラス
├── DbProgramMapperOptions.java     # グローバル設定
├── DbProgramMapperConfiguration.java # 設定クラス
├── ExecuteResult.java              # 実行結果
├── DbErrorCodes.java               # エラーコード定義
├── ParameterDirection.java         # パラメータ方向(enum)
├── annotation/
│   ├── DbProgramName.java          # プログラム名アノテーション
│   ├── DbParameterOrder.java       # パラメータ順序アノテーション
│   ├── DbParameterName.java        # パラメータ名アノテーション
│   └── DbParameterProperty.java    # パラメータ属性アノテーション
├── dialect/
│   ├── DbDialect.java              # 方言インターフェース
│   ├── SqlServerDialect.java
│   ├── PostgreSqlDialect.java
│   └── MySqlDialect.java
├── executor/
│   └── DbProgramExecutor.java      # 統一実行コンポーネント
└── internal/
    ├── DbProgramHelper.java        # ヘルパーメソッド
    └── DbProgramValidator.java     # バリデーション
```
