import type { ElMessage, ElMessageBox, ElNotification } from 'element-plus';

declare module 'vue' {
  interface ComponentCustomProperties {
    $message: typeof ElMessage
    $msgbox: typeof ElMessageBox
    $alert: typeof ElMessageBox.alert
    $confirm: typeof ElMessageBox.confirm
    $prompt: typeof ElMessageBox.prompt
    $notify: typeof ElNotification
  }
}
