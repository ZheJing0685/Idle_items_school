import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { VitePWA } from 'vite-plugin-pwa';
import { resolve } from 'path';
import AutoImport from 'unplugin-auto-import/vite';
import Components from 'unplugin-vue-components/vite';
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers';

export default defineConfig(({ mode }) => {
  const isGitHubPages = mode === 'production';
  const base = process.env.VITE_BASE || (isGitHubPages ? '/Idle_items_school/' : '/');
  return {
    base,
    esbuild: {
      charset: 'utf8',
    },
    plugins: [
      vue(),
      AutoImport({
        resolvers: [ElementPlusResolver()],
        imports: ['vue', 'vue-router', 'vue-i18n'],
        dts: 'src/auto-imports.d.ts'
      }),
      Components({
        resolvers: [ElementPlusResolver()],
        dts: 'src/components.d.ts'
      }),
      !isGitHubPages && VitePWA({
        registerType: 'autoUpdate',
        includeAssets: ['favicon.svg'],
        manifest: {
          name: '闲置物品校园交易平台',
          short_name: '校园闲置',
          description: '面向在校学生的安全高效闲置物品交易平台',
          theme_color: '#00b4c3',
          background_color: '#f5fafc',
          display: 'standalone',
          scope: '/',
          start_url: '/',
          icons: [
            {
              src: './pwa-192x192.png',
              sizes: '192x192',
              type: 'image/png'
            },
            {
              src: './pwa-512x512.png',
              sizes: '512x512',
              type: 'image/png'
            },
            {
              src: './pwa-512x512.png',
              sizes: '512x512',
              type: 'image/png',
              purpose: 'any maskable'
            }
          ]
        },
        workbox: {
          globPatterns: ['**/*.{js,css,html,svg,png,ico,woff,woff2}'],
          runtimeCaching: [
            {
              urlPattern: /^https?:\/\/.*\/api\/.*/i,
              handler: 'NetworkFirst',
              options: {
                cacheName: 'api-cache',
                expiration: {
                  maxEntries: 100,
                  maxAgeSeconds: 60 * 60
                }
              }
            },
            {
              urlPattern: /^https?:\/\/.*\/uploads\/.*/i,
              handler: 'CacheFirst',
              options: {
                cacheName: 'upload-cache',
                expiration: {
                  maxEntries: 50,
                  maxAgeSeconds: 60 * 60 * 24 * 7
                }
              }
            }
          ]
        }
      }),
    ].filter(Boolean),
    server: {
      host: '0.0.0.0',
      port: 5173,
      strictPort: true,
      allowedHosts: ['.monkeycode-ai.online'],
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
    build: {
      emptyOutDir: true,
      minify: 'esbuild',
      rollupOptions: {
        output: {
          manualChunks(id: string): string | undefined {
            if (!id.includes('node_modules')) return;
            if (/[\\/]node_modules[\\/](vue|vue-router|pinia)[\\/]/.test(id)) {
              return 'vue-vendor';
            }
            if (/[\\/]node_modules[\\/](element-plus)[\\/]/.test(id)) {
              return 'element-plus';
            }
            if (/[\\/]node_modules[\\/](echarts|zrender)[\\/]/.test(id)) {
              return 'echarts';
            }
          },
          assetFileNames: (assetInfo) => {
            if (assetInfo.name && String(assetInfo.name).endsWith('.css')) {
              return 'assets/[name]-[hash].css';
            }
            return 'assets/[name]-[hash][extname]';
          },
          chunkFileNames: 'assets/[name]-[hash].js',
          entryFileNames: 'assets/[name]-[hash].js',
        },
      },
    },
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    }
  }})
