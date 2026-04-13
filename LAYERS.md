# レイヤードアーキテクチャ解説

Spring Boot では処理を「層（レイヤー）」に分けて実装するのが一般的です。
このドキュメントでは今回追加した4つのクラスの役割と仕組みを初学者向けに解説します。

---

## 目次

1. [全体構成と処理の流れ](#1-全体構成と処理の流れ)
2. [Entity（エンティティ）](#2-entityエンティティ)
3. [Repository（リポジトリ）](#3-repositoryリポジトリ)
4. [Service（サービス）](#4-serviceサービス)
5. [Controller（コントローラー）](#5-controllerコントローラー)
6. [各ファイルの役割まとめ](#6-各ファイルの役割まとめ)
7. [リクエストの流れを追う](#7-リクエストの流れを追う)

---

## 1. 全体構成と処理の流れ

```
ブラウザ / curl
    ↓ HTTPリクエスト
┌─────────────────────┐
│  Controller         │ ← リクエストを受け取り、レスポンスを返す
│  (UserController)   │
└─────────────────────┘
    ↓ メソッド呼び出し
┌─────────────────────┐
│  Service            │ ← ビジネスロジック（何をするか）を担当
│  (UserService)      │
└─────────────────────┘
    ↓ メソッド呼び出し
┌─────────────────────┐
│  Repository         │ ← DB へのアクセスを担当
│  (UserRepository)   │
└─────────────────────┘
    ↓ SQL
┌─────────────────────┐
│  Database           │ ← データの永続化（users テーブル）
│  (PostgreSQL)       │
└─────────────────────┘
```

**なぜ層に分けるのか？**

- 各クラスの責任が明確になり、コードが読みやすくなる
- DB を変えたいときは Repository だけ、ビジネスロジックを変えたいときは Service だけ修正すればよい
- テストが書きやすくなる（層ごとに独立してテストできる）

---

## 2. Entity（エンティティ）

**ファイル:** `entity/User.java`

### 役割

DB のテーブルと Java のクラスを対応させるものです。
`@Entity` を付けることで、JPA（Hibernate）が自動的に SQL を生成してテーブルを操作してくれます。

### コード解説

```java
@Entity                    // ← このクラスが DB テーブルに対応することを宣言
@Table(name = "users")     // ← 対応するテーブル名を指定（省略するとクラス名になる）
public class User {

    @Id                                              // ← 主キー（PRIMARY KEY）
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ← 自動採番（AUTO INCREMENT）
    private Long id;

    private String name;   // ← DB の name カラムに対応
    private String email;  // ← DB の email カラムに対応

    public User() {}       // ← JPA が必要とするデフォルトコンストラクタ（必須）
    ...
}
```

### DB テーブルとの対応

| Java フィールド | DB カラム | 型 |
|---------------|----------|----|
| `id` | `id` | BIGINT（自動採番） |
| `name` | `name` | VARCHAR |
| `email` | `email` | VARCHAR |

`application.properties` に `spring.jpa.hibernate.ddl-auto=update` が設定されているため、
アプリ起動時に `users` テーブルが自動的に作成されます。

---

## 3. Repository（リポジトリ）

**ファイル:** `repository/UserRepository.java`

### 役割

DB への読み書き（CRUD）を担当する層です。
SQL を書かなくても、`JpaRepository` を継承するだけで基本的な操作がすべて使えます。

### コード解説

```java
// JpaRepository<操作するEntity, 主キーの型> を継承するだけでOK
public interface UserRepository extends JpaRepository<User, Long> {
    // ここに何も書かなくても以下のメソッドが自動で使えるようになる
}
```

### JpaRepository が提供するメソッド

| メソッド | 動作 | SQL |
|----------|------|-----|
| `findAll()` | 全件取得 | `SELECT * FROM users` |
| `findById(id)` | 1件取得 | `SELECT * FROM users WHERE id = ?` |
| `save(user)` | 新規作成 / 更新 | `INSERT` または `UPDATE` |
| `deleteById(id)` | 削除 | `DELETE FROM users WHERE id = ?` |
| `count()` | 件数取得 | `SELECT COUNT(*) FROM users` |

### カスタム検索の追加方法

メソッド名の規則に従って書くだけで SQL が自動生成されます。

```java
public interface UserRepository extends JpaRepository<User, Long> {
    // "findBy + フィールド名" で検索メソッドを追加できる
    List<User> findByName(String name);   // WHERE name = ?
    List<User> findByEmail(String email); // WHERE email = ?
}
```

---

## 4. Service（サービス）

**ファイル:** `service/UserService.java`

### 役割

ビジネスロジック（アプリケーションが「何をするか」のルール）を担当する層です。
Controller と Repository の間に入り、「どのデータを、どう処理するか」を定義します。

### コード解説

```java
@Service         // ← このクラスがサービス層であることを Spring に伝える
@Transactional   // ← メソッドをトランザクション管理下に置く
public class UserService {

    @Autowired                       // ← Spring が自動的にインスタンスを注入してくれる
    private UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll(); // Repository に処理を委譲する
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
        // Optional: 値が存在しない可能性があるときに使うコンテナ
        // 存在しない場合に null を返さず、空の Optional を返すことで NullPointerException を防ぐ
    }

    public User save(User user) {
        return userRepository.save(user);
        // id が null → INSERT（新規作成）
        // id がある → UPDATE（更新）
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
```

### @Transactional とは

DB の操作を「全部成功するか、全部なかったことにするか」をまとめて管理する仕組みです。

```
例: ユーザー作成 + メール送信 を1つのトランザクションにする

  ユーザー作成 ✓
  メール送信  ✗ ← エラー発生
  → ユーザー作成もロールバックされる（なかったことになる）
```

### @Autowired とは

`new UserRepository()` のように自分でインスタンスを作るのではなく、
Spring が管理しているインスタンスを自動で注入（セット）してくれる機能です。

---

## 5. Controller（コントローラー）

**ファイル:** `controller/UserController.java`

### 役割

HTTP リクエストを受け取り、Service を呼び出して、結果を HTTP レスポンスとして返す層です。
「何の URL にアクセスしたら何をするか」を定義します。

### コード解説

```java
@CrossOrigin(origins = "*")    // ← フロントエンドからの CORS リクエストを許可
@RestController                // ← REST API のコントローラー（戻り値を JSON で返す）
@RequestMapping("/users")      // ← このコントローラー全体の URL プレフィックス
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping             // ← GET /users
    public List<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")    // ← GET /users/{id}
    public ResponseEntity<User> findById(@PathVariable Long id) {
        // @PathVariable: URL の {id} 部分を引数に受け取る
        return userService.findById(id)
                .map(ResponseEntity::ok)                    // 見つかれば 200 OK
                .orElse(ResponseEntity.notFound().build()); // なければ 404 Not Found
    }

    @PostMapping            // ← POST /users
    public User create(@RequestBody User user) {
        // @RequestBody: リクエストの JSON ボディを User オブジェクトに変換する
        return userService.save(user);
    }

    @DeleteMapping("/{id}") // ← DELETE /users/{id}
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
```

### HTTP メソッドと CRUD の対応

| アノテーション | HTTP メソッド | 用途 |
|--------------|--------------|------|
| `@GetMapping` | GET | データの取得 |
| `@PostMapping` | POST | データの新規作成 |
| `@PutMapping` | PUT | データの更新（全体） |
| `@PatchMapping` | PATCH | データの更新（一部） |
| `@DeleteMapping` | DELETE | データの削除 |

### ResponseEntity とは

HTTP レスポンスのステータスコードも一緒に制御できるラッパークラスです。

```java
ResponseEntity.ok(user)             // 200 OK + ボディ
ResponseEntity.notFound().build()   // 404 Not Found
ResponseEntity.noContent().build()  // 204 No Content（削除成功時など）
ResponseEntity.badRequest().build() // 400 Bad Request
```

---

## 6. 各ファイルの役割まとめ

| ファイル | レイヤー | 役割 | 主なアノテーション |
|----------|---------|------|------------------|
| `entity/User.java` | Entity | DB テーブルとの対応定義 | `@Entity`, `@Table`, `@Id`, `@GeneratedValue` |
| `repository/UserRepository.java` | Repository | DB への CRUD 操作 | `JpaRepository` 継承 |
| `service/UserService.java` | Service | ビジネスロジック | `@Service`, `@Transactional`, `@Autowired` |
| `controller/UserController.java` | Controller | HTTP リクエスト/レスポンス | `@RestController`, `@RequestMapping`, `@GetMapping` など |

---

## 7. リクエストの流れを追う

### `POST /users` でユーザーを作成する場合

```
① クライアントがリクエストを送信
   POST /users
   {"name": "Alice", "email": "alice@example.com"}

② UserController.create() が呼ばれる
   @RequestBody でJSON → User オブジェクトに変換される

③ UserService.save(user) が呼ばれる
   @Transactional によりトランザクション開始

④ UserRepository.save(user) が呼ばれる
   JPA が INSERT 文を生成して実行
   INSERT INTO users (name, email) VALUES ('Alice', 'alice@example.com')

⑤ DB が id を採番して返す（例: id = 1）

⑥ 保存された User（id 付き）が Controller まで戻ってくる

⑦ JSON に変換されてレスポンス
   {"id": 1, "name": "Alice", "email": "alice@example.com"}
```

### `GET /users/1` で存在しない ID を指定した場合

```
① GET /users/999

② UserController.findById(999) が呼ばれる

③ UserService.findById(999) が呼ばれる

④ UserRepository.findById(999) が呼ばれる
   SELECT * FROM users WHERE id = 999 → 0件

⑤ Optional.empty() が返ってくる

⑥ .orElse(ResponseEntity.notFound().build()) が実行される

⑦ 404 Not Found がレスポンス
```

---

## 動作確認コマンド

```bash
# ユーザー作成
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com"}'

# 全件取得
curl http://localhost:8080/users

# 1件取得
curl http://localhost:8080/users/1

# 削除
curl -X DELETE http://localhost:8080/users/1
```
