import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:7000',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:7000',
        changeOrigin: true
      }
    }
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./tests/setup.js'],
    include: ['tests/unit/**/*.test.js'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html', 'lcov'],
      reportsDirectory: './tests/reports/coverage',
      exclude: [
        'node_modules/**',
        'tests/**',
        '**/*.config.js',
        '**/dist/**',
        '**/main.js'
      ],
      thresholds: {}
    },
    testTimeout: 10000,
    hookTimeout: 10000
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  }
})
