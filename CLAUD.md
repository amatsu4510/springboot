# プロジェクト概要

React（Vite） + Spring Boot + PostgreSQL を Docker Compose で構築する
フロントエンドとバックエンドを分離した構成とする

---

# 技術構成

* frontend: React + Vite（TypeScript）
* backend: Spring Boot（Java）
* db: PostgreSQL
* container: Docker Compose

---

# ディレクトリ構成

project-root/
├ frontend/
├ backend/
├ compose.yaml
└ CLAUDE.md

---

# 要件

## frontend（React + Vite）

* TypeScriptを使用
* ポート: 5173
* Vite開発サーバーで起動
* backendのAPIをfetchで呼び出す
* エンドポイント: http://backend:8080

## backend（Spring Boot）

* ポート: 8080
* `/hello` エンドポイントを作成
* "Hello Spring Boot" を返す
* PostgreSQLに接続可能な状態にする

## db（PostgreSQL）

* DB名: appdb
* ユーザー: appuser
* パスワード: apppass

---

# Docker構成

* docker-composeで3サービス起動

  * frontend
  * backend
  * db

## コンテナ間通信

* frontend → backend: http://backend:8080
* backend → db: jdbc:postgresql://db:5432/appdb

---

# 開発要件

* frontendはホットリロード有効
* backendはSpring DevToolsでホットリロード有効
* ローカル開発で動作確認可能

---

# 環境変数

## backend

SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/appdb
SPRING_DATASOURCE_USERNAME=appuser
SPRING_DATASOURCE_PASSWORD=apppass

## frontend

VITE_API_URL=http://backend:8080

---

# API仕様

## GET /hello

レスポンス:
"Hello Spring Boot"

---

# フロント要件

## 表示内容

* ボタンを表示
* ボタン押下でAPIを呼び出す
* レスポンスを画面に表示

---

# 出力してほしい内容

* compose.yaml
* frontend/Dockerfile
* backend/Dockerfile
* React初期コード（API呼び出し含む）
* Spring Boot初期コード
* PostgreSQL接続設定
* フォルダ構成

---

# 制約

* 学習目的のためシンプル構成とする
* 不要なライブラリは追加しない
* 初心者でも理解できるようコメントを付けること
