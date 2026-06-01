import type { App } from 'vue';
import ElementPlus, { ElMessage, ElMessageBox, ElNotification, ElLoading } from 'element-plus';
import zhCn from 'element-plus/es/locale/lang/zh-cn';

const plugins = [
  ElLoading,
];

export function setupElementPlus(app: App) {
  // 全局安装 Element Plus，传入中文语言包配置
  app.use(ElementPlus, { locale: zhCn });

  plugins.forEach(plugin => app.use(plugin));

  app.config.globalProperties.$message = ElMessage;
  app.config.globalProperties.$msgbox = ElMessageBox;
  app.config.globalProperties.$alert = ElMessageBox.alert;
  app.config.globalProperties.$confirm = ElMessageBox.confirm;
  app.config.globalProperties.$prompt = ElMessageBox.prompt;
  app.config.globalProperties.$notify = ElNotification;
}
