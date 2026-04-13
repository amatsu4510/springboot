# ─── Stage 1: ビルド ──────────────────────────────────────────────────────────
# Maven と JDK 21 が入った公式イメージでコンパイル・パッケージングを行う
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# コンテナ内の作業ディレクトリを設定
WORKDIR /app

# pom.xml だけ先にコピーして依存をダウンロードする
# pom.xml が変わらない限りこのレイヤーはキャッシュされるため、再ビルドが速くなる
COPY pom.xml .
RUN mvn dependency:go-offline -B

# ソースコードをコピーして JAR をビルドする（テストはスキップ）
COPY src ./src
RUN mvn package -DskipTests -B

# ─── Stage 2: 実行 ────────────────────────────────────────────────────────────
# 実行時は Maven 不要なので、軽量な JDK 21 ランタイムイメージを使う
FROM eclipse-temurin:21-jdk

WORKDIR /app

# ビルドステージでコンパイルした JAR だけをコピー
COPY --from=builder /app/target/*.jar app.jar

# Spring Boot がリッスンするポートを公開
EXPOSE 8080

# アプリケーションを起動
ENTRYPOINT ["java", "-jar", "app.jar"]
