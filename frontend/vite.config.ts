import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],

  server: {
    // 0.0.0.0 にバインドすることで Docker がポートを外部に公開できる
    host: '0.0.0.0',
    port: 5173,

    // Vite プロキシ: ブラウザからの /api/* を backend コンテナに転送する
    // → ブラウザは相対URLを呼ぶだけなので CORS の問題が起きない
    proxy: {
      '/api': {
        target: 'http://backend:8080',
        changeOrigin: true,
        // /api プレフィックスを除去してから転送: /api/hello → /hello
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
