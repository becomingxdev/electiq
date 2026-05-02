import process from 'node:process'
import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [react()],
    envPrefix: ['VITE_', 'REACT_APP_'],
    server: {
      port: 5173,
      proxy: {
        // In local dev, proxy /api/v1 to the Spring Boot backend.
        // This avoids CORS entirely: the browser only sees localhost:5173.
        '/api/v1': {
          target: env.VITE_PROXY_TARGET || 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
        },
      },
    },
  }
})

