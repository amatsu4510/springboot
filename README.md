# 開発環境ガイド

React + Spring Boot + PostgreSQL の Docker Compose 構成を使った学習用プロジェクトです。

---

## 目次

1. [プロジェクト概要](#1-プロジェクト概要)
2. [ディレクトリ構成](#2-ディレクトリ構成)
3. [技術スタックとバージョン](#3-技術スタックとバージョン)
4. [セットアップ・起動手順](#4-セットアップ起動手順)
5. [各サービスの説明](#5-各サービスの説明)
6. [API仕様](#6-api仕様)
7. [ツールのアップデート方法](#7-ツールのアップデート方法)

---

## 1. プロジェクト概要

フロントエンド（React + Vite）とバックエンド（Spring Boot）を分離した構成で、
PostgreSQL をデータベースとして使用します。すべてのサービスは Docker Compose で
一括管理されます。

| サービス  | 技術                | ポート |
|-----------|---------------------|--------|
| frontend  | React + Vite (TypeScript) | 5173   |
| backend   | Spring Boot 3 (Java 21)   | 8080   |
| db        | PostgreSQL 16             | 5432   |

---

## 2. ディレクトリ構成

```
/work/springboot/
├── compose.yaml                    # Docker Compose 設定
├── CLAUD.md                        # Claude Code 向けプロジェクト仕様
├── README.md                       # このファイル
├── frontend/
│   ├── Dockerfile
│   ├── package.json
│   ├── vite.config.ts              # Vite 設定（プロキシ設定含む）
│   ├── tsconfig.json
│   ├── index.html
│   └── src/
│       ├── main.tsx                # エントリーポイント
│       └── App.tsx                 # メインコンポーネント（API呼び出し含む）
└── backend/
    ├── Dockerfile
    ├── pom.xml                     # Maven 依存関係定義
    └── src/
        └── main/
            ├── java/com/example/demo/
            │   ├── DemoApplication.java    # Spring Boot エントリーポイント
            │   └── HelloController.java    # REST コントローラー
            └── resources/
                └── application.properties  # DB接続・JPA設定
```

---

## 3. 技術スタックとバージョン

### ホスト環境にインストール済みのツール

| ツール      | バージョン        | 用途                               |
|-------------|-------------------|------------------------------------|
| Java (OpenJDK) | **21.0.10**    | Spring Boot のビルド・実行         |
| Maven       | **3.8.7**         | Java プロジェクトのビルドツール    |
| Node.js     | **20.20.1**       | フロントエンド開発・npm 実行       |
| npm         | **10.8.2**        | Node.js パッケージマネージャー     |
| Python      | **3.12.3**        | スクリプト・ツール用               |
| Docker      | **28.5.1**        | コンテナ管理                       |
| Git         | **2.43.0**        | バージョン管理                     |

### プロジェクト内で使用しているライブラリのバージョン

| ライブラリ             | バージョン  | 定義ファイル       |
|------------------------|-------------|--------------------|
| Spring Boot            | 3.2.5       | `backend/pom.xml`  |
| spring-boot-starter-web | 3.2.5 (親から継承) | `backend/pom.xml` |
| spring-boot-starter-data-jpa | 3.2.5 (親から継承) | `backend/pom.xml` |
| PostgreSQL JDBC ドライバー | 親から継承 | `backend/pom.xml` |
| React                  | ^18.3.1     | `frontend/package.json` |
| Vite                   | ^5.4.1      | `frontend/package.json` |
| TypeScript             | ^5.4.5      | `frontend/package.json` |
| PostgreSQL (Docker)    | 16-alpine   | `compose.yaml`     |

---

## 4. セットアップ・起動手順

### 前提条件

- Docker が起動していること (`docker info` で確認)
- ポート 5173、8080、5432 が空いていること

### 起動

```bash
# プロジェクトルートに移動
cd /work/springboot

# コンテナをビルドして起動（初回はイメージのビルドに数分かかる）
docker compose up --build

# バックグラウンドで起動する場合
docker compose up --build -d
```

### 動作確認

起動後、以下の URL にアクセスします。

| URL                          | 内容                        |
|------------------------------|-----------------------------|
| http://localhost:5173        | React フロントエンド         |
| http://localhost:8080/hello  | Spring Boot API（直接アクセス）|
| localhost:5432               | PostgreSQL（DB クライアント用）|

### 停止

```bash
# コンテナを停止
docker compose down

# ボリューム（DBデータ）も削除して完全リセット
docker compose down -v
```

### ログ確認

```bash
# 全サービスのログを表示
docker compose logs -f

# 特定サービスのみ
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f db
```

---

## 5. 各サービスの説明

### フロントエンド（React + Vite）

- **ファイル:** `frontend/src/App.tsx`
- ボタンをクリックすると `/api/hello` を fetch します
- `vite.config.ts` のプロキシ設定により `/api/*` は `http://backend:8080/*` に転送されます（CORS 回避）
- ホットリロード有効（ファイル保存で自動反映）

```
ブラウザ → /api/hello → Vite プロキシ → http://backend:8080/hello
```

### バックエンド（Spring Boot）

- **エントリーポイント:** `DemoApplication.java`
- **コントローラー:** `HelloController.java`
- `/hello` エンドポイントが `"Hello Spring Boot"` を返します
- Spring DevTools によりクラスファイル変更時に自動再起動します
- PostgreSQL には JPA（Hibernate）で接続します

### データベース（PostgreSQL）

| 設定     | 値       |
|----------|----------|
| DB名     | appdb    |
| ユーザー | appuser  |
| パスワード | apppass |
| ポート   | 5432     |

DB クライアント（DBeaver 等）からの接続例:

```
Host:     localhost
Port:     5432
Database: appdb
User:     appuser
Password: apppass
```

---

## 6. API仕様

### GET /hello

バックエンドが正常に起動しているか確認するシンプルなエンドポイントです。

**リクエスト:**
```
GET http://localhost:8080/hello
```

**レスポンス:**
```
Hello Spring Boot
```

---

## 7. ツールのアップデート方法

### Java (OpenJDK)

現在: **21.0.10** (Ubuntu パッケージ)

```bash
# 利用可能なバージョンを確認
apt list --installed 2>/dev/null | grep openjdk

# Ubuntu パッケージを最新に更新
sudo apt update
sudo apt upgrade openjdk-21-jdk

# 別のメジャーバージョンをインストールする場合（例: Java 24）
sudo apt install openjdk-24-jdk

# 複数バージョンが入っている場合はデフォルトを切り替える
sudo update-alternatives --config java
```

> **注意:** Spring Boot 3.x は Java 17 以上が必要です。Java 21 は LTS バージョンのため、
> 特別な理由がなければ現状維持を推奨します。

---

### Maven

現在: **3.8.7**

Ubuntu の apt リポジトリは最新版でない場合があるため、公式サイトから手動インストールが確実です。

```bash
# 最新バージョンを確認（2025年時点では 3.9.x が最新）
# https://maven.apache.org/download.cgi

# 例: 3.9.9 をインストール
VERSION=3.9.9
wget https://downloads.apache.org/maven/maven-3/${VERSION}/binaries/apache-maven-${VERSION}-bin.tar.gz
sudo tar -xzf apache-maven-${VERSION}-bin.tar.gz -C /opt
sudo ln -sfn /opt/apache-maven-${VERSION} /opt/maven

# 環境変数を設定（~/.bashrc または ~/.zshrc に追記）
export MAVEN_HOME=/opt/maven
export PATH=$MAVEN_HOME/bin:$PATH

# 確認
mvn -version
```

---

### Node.js

現在: **20.20.1**

[nvm (Node Version Manager)](https://github.com/nvm-sh/nvm) を使うと複数バージョンの管理が簡単です。

```bash
# nvm がインストールされていない場合
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash
source ~/.bashrc

# 利用可能なバージョン一覧
nvm ls-remote --lts

# 特定バージョンをインストール
nvm install 22        # Node.js 22 (LTS) をインストール
nvm use 22            # 使用バージョンを切り替え
nvm alias default 22  # デフォルトに設定

# npm もあわせて最新にする
npm install -g npm@latest

# 確認
node --version
npm --version
```

> **注意:** `frontend/Dockerfile` は `node:20-alpine` イメージを使用しています。
> ホスト側のバージョンを変えても Docker ビルドには影響しません。
> Docker イメージのバージョンを変える場合は `frontend/Dockerfile` の `FROM` 行を編集してください。

---

### npm

現在: **10.8.2**

```bash
# npm 単体でアップデート
npm install -g npm@latest

# 確認
npm --version
```

---

### Python

現在: **3.12.3**

```bash
# Ubuntu パッケージで更新
sudo apt update
sudo apt upgrade python3

# 特定バージョンを追加インストールする場合（deadsnakes PPA を使用）
sudo add-apt-repository ppa:deadsnakes/ppa
sudo apt update
sudo apt install python3.13

# 確認
python3 --version
```

---

### Docker

現在: **28.5.1**

```bash
# Docker 公式のアップデート手順
sudo apt update
sudo apt upgrade docker-ce docker-ce-cli containerd.io docker-compose-plugin

# または Docker の公式スクリプトで最新版にアップデート
# https://docs.docker.com/engine/install/ubuntu/

# 確認
docker --version
docker compose version
```

---

### Git

現在: **2.43.0**

```bash
# Ubuntu の標準リポジトリで更新
sudo apt update
sudo apt upgrade git

# より新しいバージョンが必要な場合は PPA を使用
sudo add-apt-repository ppa:git-core/ppa
sudo apt update
sudo apt install git

# 確認
git --version
```

---

## よくある操作

### Docker イメージを再ビルドする

ソースコードを変更した後にイメージを作り直す場合:

```bash
# キャッシュなしで完全再ビルド
docker compose build --no-cache

# 特定サービスのみ再ビルド
docker compose build --no-cache backend
```

### コンテナの状態を確認する

```bash
docker compose ps
```

### バックエンドをローカル（Docker なし）で起動する

```bash
cd /work/springboot/backend
mvn spring-boot:run
```

> DB が別途起動していない場合はエラーになります。DB だけ Docker で起動する方法:
> ```bash
> docker compose up db
> ```

### フロントエンドをローカル（Docker なし）で起動する

```bash
cd /work/springboot/frontend
npm install
npm run dev
```
