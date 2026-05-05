import js from '@eslint/js';
import pluginVue from 'eslint-plugin-vue';

export default [
  {
    files: ['**/*.js', '**/*.vue'],
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      globals: {
        browser: true,
        node: true,
        document: 'readonly',
        window: 'readonly',
      },
    },
    plugins: {
      vue: pluginVue,
    },
    rules: {
      ...js.configs.recommended.rules,
      ...pluginVue.configs.base.rules,
      
      // 语法检查
      'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
      'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
      'no-unused-vars': ['warn', { argsIgnorePattern: '^_' }],
      'no-duplicate-imports': 'error',
      'no-var': 'error',
      
      // 代码风格
      'prefer-const': 'warn',
      'no-trailing-spaces': 'warn',
      'semi': ['warn', 'always'],
      'quotes': ['warn', 'single', { avoidEscape: true }],
      'indent': ['warn', 2],
      'comma-dangle': ['warn', 'always-multiline'],
      'object-curly-spacing': ['warn', 'always'],
      'array-bracket-spacing': ['warn', 'never'],
      
      // Vue 规则
      'vue/no-unused-vars': 'warn',
      'vue/require-v-for-key': 'error',
      'vue/no-dupe-keys': 'error',
    },
  },
  {
    ignores: [
      'node_modules/',
      'dist/',
      'build/',
      'coverage/',
      'test-results/',
      '.next/',
      '*.d.ts',
    ],
  },
]
