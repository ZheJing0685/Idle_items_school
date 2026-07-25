import { createApp } from 'vue';
import 'element-plus/dist/index.css';
import 'cropperjs/dist/cropper.min.css';
import './styles/element-theme.css';
import './styles/components/button.css';
import './styles/components/form.css';
import router from './router';
import pinia from './store';
import i18n from './locale';
import './style.css';
import './styles/dark-mode.css';
import App from './App.vue';
// import { setupGlobalErrorHandler } from '@/utils/error/errorHandler';
import CategorySearch from './components/common/CategorySearch.vue';

// setupGlobalErrorHandler();

const app = createApp(App);
app.use(router);
app.use(pinia);
app.use(i18n);

/* 全局注册常用组件 */
app.component('CategorySearch', CategorySearch);

app.mount('#app');
