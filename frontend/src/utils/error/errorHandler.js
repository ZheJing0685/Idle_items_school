import { ElMessage, ElMessageBox } from 'element-plus';
import { classifyError, ErrorType } from './errorTypes';
import router from '../../router';

class ErrorHandler {
  static handle(error, options = {}) {
    if (!error) return;

    const errorType = classifyError(error);
    const message = error.message || this.getErrorMessage(errorType);

    if (options.silent) return;

    this.showMessage({
      type: errorType,
      message,
      options,
    });

    if (errorType === ErrorType.AUTHENTICATION_ERROR) {
      this.handleAuthError(router);
    }

    return error;
  }

  static showMessage(error) {
    const { type, message, options = {} } = error;

    switch (type) {
      case ErrorType.NETWORK_ERROR:
      case ErrorType.TIMEOUT_ERROR:
      case ErrorType.SERVER_ERROR:
        ElMessage({
          message,
          type: 'error',
          duration: options.duration || 5000,
        });
        break;
      case ErrorType.AUTHENTICATION_ERROR:
        break;
      case ErrorType.AUTHORIZATION_ERROR:
        ElMessage({
          message,
          type: 'warning',
          duration: options.duration || 3000,
        });
        break;
      case ErrorType.VALIDATION_ERROR:
      case ErrorType.CLIENT_ERROR:
        ElMessage({
          message,
          type: 'warning',
          duration: options.duration || 3000,
        });
        break;
      case ErrorType.NOT_FOUND_ERROR:
        ElMessage({
          message,
          type: 'info',
          duration: options.duration || 3000,
        });
        break;
      default:
        ElMessage({
          message,
          type: 'error',
          duration: options.duration || 3000,
        });
    }
  }

  static handleAuthError(router) {
    this.clearAuthStorage();
    ElMessageBox.alert('登录已过期，请重新登录', '提示', {
      confirmButtonText: '确定',
      callback: () => {
        router.push('/login');
      },
    });
  }

  static clearAuthStorage() {
    // 清除Cookie中的token
    document.cookie =
      'user_token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';

    // 清除localStorage中的数据
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('refreshToken');

    // 清除带命名空间的存储
    localStorage.removeItem('user:refreshToken');
    localStorage.removeItem('user:user');
    localStorage.removeItem('user:lastLoginTime');
    localStorage.removeItem('user:rememberMe');
  }

  static isAuthError(error) {
    const errorType = classifyError(error);
    return errorType === ErrorType.AUTHENTICATION_ERROR;
  }

  static isNetworkError(error) {
    const errorType = classifyError(error);
    return (
      errorType === ErrorType.NETWORK_ERROR ||
      errorType === ErrorType.TIMEOUT_ERROR
    );
  }

  static getErrorMessage(errorType) {
    const messages = {
      [ErrorType.NETWORK_ERROR]: '网络连接失败，请检查网络',
      [ErrorType.TIMEOUT_ERROR]: '请求超时，请稍后重试',
      [ErrorType.VALIDATION_ERROR]: '输入验证失败',
      [ErrorType.AUTHENTICATION_ERROR]: '登录已过期，请重新登录',
      [ErrorType.AUTHORIZATION_ERROR]: '权限不足',
      [ErrorType.NOT_FOUND_ERROR]: '资源不存在',
      [ErrorType.CONFLICT_ERROR]: '资源冲突',
      [ErrorType.SERVER_ERROR]: '服务器错误，请稍后重试',
      [ErrorType.CLIENT_ERROR]: '请求失败',
      [ErrorType.UNKNOWN_ERROR]: '网络异常，请稍后重试',
    };
    return messages[errorType] || messages[ErrorType.UNKNOWN_ERROR];
  }
}

export default ErrorHandler;
