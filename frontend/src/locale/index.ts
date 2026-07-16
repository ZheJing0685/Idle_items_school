import { createI18n } from 'vue-i18n';
import zhCN from './zh-CN';
import en from './en';

export type LocaleKey = 'zh-CN' | 'en';

const i18n = createI18n({
  legacy: false,
  locale: 'zh-CN',
  fallbackLocale: 'zh-CN',
  globalInjection: true,
  messages: {
    'zh-CN': zhCN,
    'en': en,
  },
  missingWarn: false,
  fallbackWarn: false,
});

export default i18n;
