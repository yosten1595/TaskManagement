import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // ポートは固定。使用中でも別ポートへ逃げず、起動に失敗させる
    port: 5173,
    strictPort: true,
    // 開発中は /api をバックエンドへ転送する（CORS 設定を不要にする）
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
