# バッチ処理ガイド: @Scheduled と Spring Batch

このプロジェクトでは 2 種類のバッチ処理を実装しています。
それぞれの仕組みと使い分けを説明します。

---

## 目次

1. [@Scheduled による定期実行](#1-scheduled-による定期実行)
2. [Spring Batch によるバッチ処理](#2-spring-batch-によるバッチ処理)
3. [2つのアプローチの比較](#3-2つのアプローチの比較)
4. [このプロジェクトでの実装](#4-このプロジェクトでの実装)

---

## 1. @Scheduled による定期実行

### 概要

`@Scheduled` は Spring Framework が提供するアノテーションで、
メソッドを**定期的に自動実行**するための仕組みです。
設定が少なく、シンプルな繰り返し処理に向いています。

### 有効化の方法

`@EnableScheduling` をアプリケーションクラスに付けることで機能が有効になります。

```java
// DemoApplication.java
@SpringBootApplication
@EnableScheduling   // ← これを付けると @Scheduled が動くようになる
public class DemoApplication { ... }
```

### 実行タイミングの指定方法

`@Scheduled` には 3 種類の実行タイミング指定があります。

#### fixedRate（固定レート）

```java
@Scheduled(fixedRate = 5000)
public void run() { ... }
```

- **前回の開始時刻**から指定ミリ秒後に次を実行する
- 処理が遅延しても次の実行は予定通りスケジュールされる

```
実行開始 ──── 5秒 ──── 実行開始 ──── 5秒 ──── 実行開始
（処理中）               （処理中）               （処理中）
```

#### fixedDelay（固定遅延）

```java
@Scheduled(fixedDelay = 5000)
public void run() { ... }
```

- **前回の終了時刻**から指定ミリ秒後に次を実行する
- 処理時間が長くなっても次の実行は終わってから始まる

```
実行開始 → 処理完了 ──── 5秒 ──── 実行開始 → 処理完了 ──── 5秒 ────
```

#### cron（Cron 式）

```java
@Scheduled(cron = "0 0 9 * * MON-FRI")  // 平日の毎朝 9:00
public void run() { ... }
```

Cron 式の書式: `秒 分 時 日 月 曜日`

| 例 | 意味 |
|----|------|
| `0 * * * * *` | 毎分 0 秒 |
| `0 0 * * * *` | 毎時 0 分 0 秒 |
| `0 0 9 * * *` | 毎日 9:00:00 |
| `0 0 9 * * MON` | 毎週月曜 9:00:00 |
| `0 0 9 1 * *` | 毎月 1 日 9:00:00 |

### 仕組み

```
Spring コンテナ起動
       ↓
@EnableScheduling がスケジューラースレッドプールを初期化
       ↓
@Scheduled が付いたメソッドを登録
       ↓
指定タイミングで別スレッドからメソッドを呼び出す
```

デフォルトはシングルスレッドで動作します。
並列実行が必要な場合は `@Async` と組み合わせるか、
スレッドプールの設定を追加します。

---

## 2. Spring Batch によるバッチ処理

### 概要

Spring Batch は**大量データの一括処理**に特化したフレームワークです。
処理の進捗管理・再実行・スキップ・リトライなどの機能が組み込まれています。

### 主要コンポーネント

```
┌─────────────────────────────────────────────────────────┐
│  Job（バッチ処理全体の単位）                              │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Step（処理の 1 ステップ）                         │   │
│  │  ┌──────────┐  ┌───────────┐  ┌──────────────┐  │   │
│  │  │  Reader  │→ │ Processor │→ │    Writer    │  │   │
│  │  │（読み込み）│  │（加工・変換）│  │（書き出し）    │  │   │
│  │  └──────────┘  └───────────┘  └──────────────┘  │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
         ↕ 実行履歴・進捗を記録
┌─────────────────────────────────────────────────────────┐
│  JobRepository（メタデータDB）                            │
└─────────────────────────────────────────────────────────┘
```

#### Job

バッチ処理全体の定義です。1 つ以上の Step を持ちます。

```java
@Bean
Job sampleJob(JobRepository jobRepository, Step sampleStep) {
    return new JobBuilder("sampleJob", jobRepository)
            .start(sampleStep)
            .build();
}
```

#### Step

Job を構成する処理単位です。`Reader → Processor → Writer` の流れを定義します。

```java
@Bean
Step sampleStep(JobRepository jobRepository, PlatformTransactionManager tm) {
    return new StepBuilder("sampleStep", jobRepository)
            .<String, String>chunk(5, tm)  // 5件ずつまとめて処理
            .reader(itemReader())
            .processor(itemProcessor())
            .writer(itemWriter())
            .build();
}
```

#### ItemReader

処理対象データを 1 件ずつ読み込みます。
`null` を返したタイミングで読み込み終了と判断されます。

```java
// インメモリリストから読み込む例
@Bean
ListItemReader<String> itemReader() {
    return new ListItemReader<>(List.of("item1", "item2", "item3"));
}
```

よく使われる組み込み Reader:

| クラス | 読み込み元 |
|--------|-----------|
| `ListItemReader` | インメモリリスト |
| `FlatFileItemReader` | CSV / テキストファイル |
| `JdbcCursorItemReader` | DB（カーソル方式） |
| `JpaPagingItemReader` | DB（JPA ページング方式） |

#### ItemProcessor

Reader から受け取った 1 件を加工・変換します。
`null` を返すとその件はスキップされます。

```java
@Bean
ItemProcessor<String, String> itemProcessor() {
    return item -> item.toUpperCase();  // 大文字に変換
}
```

#### ItemWriter

Processor の出力を `chunk` サイズ分まとめて受け取り、書き出します。

```java
@Bean
ItemWriter<String> itemWriter() {
    return chunk -> chunk.getItems().forEach(System.out::println);
}
```

よく使われる組み込み Writer:

| クラス | 書き出し先 |
|--------|-----------|
| `FlatFileItemWriter` | CSV / テキストファイル |
| `JdbcBatchItemWriter` | DB（JDBC バッチ） |
| `JpaItemWriter` | DB（JPA） |

### Chunk 指向処理の流れ

`chunk(5)` の場合、5 件ごとにまとめてコミットします。

```
Reader → item1 → Processor → ITEM1 ─┐
Reader → item2 → Processor → ITEM2  │ 5件たまったら
Reader → item3 → Processor → ITEM3  │ Writer に渡して
Reader → item4 → Processor → ITEM4  │ 一括コミット
Reader → item5 → Processor → ITEM5 ─┘
Reader → item6 → Processor → ITEM6 ─┐
...                                   │ 再度 5件まとめる
Reader → item10 → Processor → ITEM10─┘
Reader → null（終了）
```

途中でエラーが起きた場合、コミット済みの chunk までは保存されます。

### JobRepository（メタデータ管理）

Spring Batch は実行履歴を DB に自動記録します。
`spring.batch.jdbc.initialize-schema=always` を設定すると起動時にテーブルが作成されます。

| テーブル名 | 内容 |
|-----------|------|
| `BATCH_JOB_INSTANCE` | Job の定義（名前・パラメータ） |
| `BATCH_JOB_EXECUTION` | Job の実行履歴（開始・終了・ステータス） |
| `BATCH_STEP_EXECUTION` | Step の実行履歴（処理件数・スキップ数など） |
| `BATCH_JOB_EXECUTION_PARAMS` | Job 実行時のパラメータ |

Adminer（http://localhost:8081）でこれらのテーブルを確認できます。

### JobParameters と再実行

Spring Batch は**同一パラメータの Job は 1 度しか実行できない**仕様です。
再実行可能にするには、実行ごとに異なるパラメータを渡します。

```java
JobParameters params = new JobParametersBuilder()
        .addLong("time", System.currentTimeMillis())  // 毎回異なる値
        .toJobParameters();
jobLauncher.run(sampleJob, params);
```

---

## 3. 2つのアプローチの比較

| 項目 | @Scheduled | Spring Batch |
|------|-----------|-------------|
| セットアップ | 簡単（アノテーション 1 つ） | pom.xml + 設定クラスが必要 |
| 向いている処理量 | 少量〜中量 | 大量データ |
| 実行履歴の管理 | なし | DB に自動記録 |
| 途中失敗時の再開 | 非対応 | 続きから再実行可能 |
| スキップ・リトライ | 自前実装が必要 | 組み込みサポートあり |
| トランザクション | 自前管理 | chunk 単位で自動管理 |
| 処理の進捗監視 | 困難 | メタデータ DB で確認可能 |
| 学習コスト | 低い | 高い |

### 使い分けの目安

```
シンプルな定期処理（通知送信・キャッシュ更新など）
  → @Scheduled

大量データの一括処理（CSVインポート・日次集計など）
  → Spring Batch
```

---

## 4. このプロジェクトでの実装

### ファイル構成

```
src/main/java/com/example/demo/
├── DemoApplication.java    # @EnableScheduling でスケジューリングを有効化
├── BatchJob.java           # @Scheduled サンプル（5秒ごとにログ出力）
├── BatchConfig.java        # Spring Batch の Job/Step/Reader/Processor/Writer 定義
└── BatchScheduler.java     # @Scheduled で Spring Batch Job を 30 秒ごとに起動
```

### 処理フロー

```
起動後 5 秒ごと（BatchJob.java）
  └→ "＠Scheduledで5秒ごとに実行" をコンソールに出力

起動後 30 秒ごと（BatchScheduler.java）
  └→ JobLauncher.run(sampleJob)
       └→ sampleStep（chunk=5）
            ├→ ItemReader  : item1〜item10 を順番に読み込む
            ├→ ItemProcessor: 大文字に変換（item1 → ITEM1）
            └→ ItemWriter  : 5件ずつまとめてコンソールに出力
```

### ログ出力例

```
[BatchScheduler] Spring Batch Job を起動します
  [Processor] item1 → ITEM1
  [Processor] item2 → ITEM2
  [Processor] item3 → ITEM3
  [Processor] item4 → ITEM4
  [Processor] item5 → ITEM5
  [Writer] 5件を書き込み:
    → ITEM1
    → ITEM2
    → ITEM3
    → ITEM4
    → ITEM5
  [Processor] item6 → ITEM6
  ...（続く）
[BatchScheduler] Spring Batch Job が完了しました
```

### 動作確認

```bash
# コンテナを起動してログを確認
docker compose up --build
docker compose logs -f backend

# Adminer で Spring Batch メタデータを確認
# http://localhost:8081 にアクセス → BATCH_JOB_EXECUTION テーブルを確認
```
