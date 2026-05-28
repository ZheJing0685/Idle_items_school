import { createApp } from 'vue';
import { setupElementPlus } from './plugins/element-plus';
import 'element-plus/dist/index.css';
import './styles/element-theme.css';
import router from './router';
import pinia from './store';
import i18n from './locale';
import './style.css';
import './styles/dark-mode.css';
import App from './App.vue';
import { setupGlobalErrorHandler } from '@/utils/error/errorHandler';

setupGlobalErrorHandler();

const app = createApp(App);
setupElementPlus(app);
app.use(router);
app.use(pinia);
app.use(i18n);
app.mount('#app');
