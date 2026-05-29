import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'jsdom',
    include: ['tests/unit/**/*.test.{ts,tsx}'],
    exclude: ['tests/e2e/**', 'node_modules/**', 'dist/**'],
    setupFiles: ['tests/setup.ts'],
    testTimeout: 10000,
    hookTimeout: 10000,
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html', 'lcov'],
      reportsDirectory: './coverage',
      include: ['src/**/*.{ts,js,vue}'],
      exclude: [
        'node_modules/',
        'dist/',
        'src/**/*.d.ts',
        'src/main.ts',
        'src/App.vue',
        'src/plugins/',
        'src/locale/',
      ],
      thresholds: {
        statements: 13,
        branches: 10,
        functions: 40,
        lines: 13,
      },
    },
    reporters: ['verbose'],
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
})
