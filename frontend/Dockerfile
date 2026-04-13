# 公式 Node.js 20 イメージを使用（LTS版、ホストの Node バージョンと一致）
FROM node:20-alpine

# コンテナ内の作業ディレクトリを設定
WORKDIR /app

# package.json を先にコピーして npm install をキャッシュする
# 依存関係が変わらない限りこのレイヤーはキャッシュされる
COPY package.json package-lock.json* ./

# 全依存をインストール（Vite に必要な devDependencies も含む）
RUN npm install

# 残りのソースコードをコピー
COPY . .

# Vite 開発サーバーのポートを公開
EXPOSE 5173

# Vite 開発サーバーを起動
# --host は 0.0.0.0 にバインドするために必要（Docker がトラフィックを転送できる）
CMD ["npm", "run", "dev"]
