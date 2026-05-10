// 统一错误处理逻辑

// 错误码映射
export const ERROR_CODES = {
  // 系统错误
  SYSTEM_ERROR: 50000,
  NETWORK_ERROR: 50001,
  TIMEOUT_ERROR: 50002,

  // 认证错误
  UNAUTHORIZED: 40100,
  TOKEN_EXPIRED: 40101,
  INVALID_TOKEN: 40102,

  // 权限错误
  FORBIDDEN: 40300,
  PERMISSION_DENIED: 40301,

  // 资源错误
  NOT_FOUND: 40400,
  RESOURCE_NOT_FOUND: 40401,

  // 业务错误
  BAD_REQUEST: 40000,
  VALIDATION_ERROR: 40001,
  DUPLICATE_RESOURCE: 40002,
  INVALID_PARAMS: 40003,

  // 认证相关错误
  LOGIN_FAILED: 40004,
  REGISTER_FAILED: 40005,
  USER_NOT_FOUND: 40006,
  ACCOUNT_DISABLED: 40007,

  // 物品相关错误
  ITEM_NOT_FOUND: 40008,
  CREATE_ITEM_FAILED: 40009,
  UPDATE_ITEM_FAILED: 40010,
  ITEM_OFF_SHELF_FAILED: 40011,

  // 订单相关错误
  ORDER_NOT_FOUND: 40012,
  CREATE_ORDER_FAILED: 40013,
  CANCEL_ORDER_FAILED: 40014,
  PAY_ORDER_FAILED: 40015,
  SHIP_ORDER_FAILED: 40016,
  CONFIRM_ORDER_FAILED: 40017,

  // 评价相关错误
  CREATE_REVIEW_FAILED: 40018,
  REVIEW_NOT_FOUND: 40019,

  // 收藏相关错误
  ADD_FAVORITE_FAILED: 40020,
  REMOVE_FAVORITE_FAILED: 40021,

  // 分类相关错误
  CATEGORY_NOT_FOUND: 40022,

  // 未知错误
  UNKNOWN_ERROR: 99999,
};

// 错误信息映射
const ERROR_MESSAGES = {
  [ERROR_CODES.SYSTEM_ERROR]: '服务器错误，请稍后重试',
  [ERROR_CODES.NETWORK_ERROR]: '网络连接失败，请检查网络',
  [ERROR_CODES.TIMEOUT_ERROR]: '请求超时，请稍后重试',
  [ERROR_CODES.UNAUTHORIZED]: '请先登录',
  [ERROR_CODES.TOKEN_EXPIRED]: '登录已过期，请重新登录',
  [ERROR_CODES.INVALID_TOKEN]: '无效的登录凭证',
  [ERROR_CODES.FORBIDDEN]: '权限不足',
  [ERROR_CODES.PERMISSION_DENIED]: '没有操作权限',
  [ERROR_CODES.NOT_FOUND]: '资源不存在',
  [ERROR_CODES.RESOURCE_NOT_FOUND]: '请求的资源不存在',
  [ERROR_CODES.BAD_REQUEST]: '请求参数错误',
  [ERROR_CODES.VALIDATION_ERROR]: '数据验证失败',
  [ERROR_CODES.DUPLICATE_RESOURCE]: '资源已存在',
  [ERROR_CODES.INVALID_PARAMS]: '无效的参数',
  [ERROR_CODES.LOGIN_FAILED]: '用户名或密码错误',
  [ERROR_CODES.REGISTER_FAILED]: '注册失败',
  [ERROR_CODES.USER_NOT_FOUND]: '用户不存在',
  [ERROR_CODES.ACCOUNT_DISABLED]: '账号已被禁用',
  [ERROR_CODES.ITEM_NOT_FOUND]: '物品不存在',
  [ERROR_CODES.CREATE_ITEM_FAILED]: '创建物品失败',
  [ERROR_CODES.UPDATE_ITEM_FAILED]: '更新物品失败',
  [ERROR_CODES.ITEM_OFF_SHELF_FAILED]: '下架物品失败',
  [ERROR_CODES.ORDER_NOT_FOUND]: '订单不存在',
  [ERROR_CODES.CREATE_ORDER_FAILED]: '创建订单失败',
  [ERROR_CODES.CANCEL_ORDER_FAILED]: '取消订单失败',
  [ERROR_CODES.PAY_ORDER_FAILED]: '支付订单失败',
  [ERROR_CODES.SHIP_ORDER_FAILED]: '发货失败',
  [ERROR_CODES.CONFIRM_ORDER_FAILED]: '确认收货失败',
  [ERROR_CODES.CREATE_REVIEW_FAILED]: '创建评价失败',
  [ERROR_CODES.REVIEW_NOT_FOUND]: '评价不存在',
  [ERROR_CODES.ADD_FAVORITE_FAILED]: '添加收藏失败',
  [ERROR_CODES.REMOVE_FAVORITE_FAILED]: '移除收藏失败',
  [ERROR_CODES.CATEGORY_NOT_FOUND]: '分类不存在',
  [ERROR_CODES.UNKNOWN_ERROR]: '网络异常，请稍后重试',
};

// 错误处理类
class ErrorHandler {
  // 获取错误信息
  static getErrorMessage(errorCode) {
    return (
      ERROR_MESSAGES[errorCode] || ERROR_MESSAGES[ERROR_CODES.UNKNOWN_ERROR]
    );
  }

  // 处理HTTP错误
  static handleHttpError(error) {
    if (error.response) {
      const { status, data } = error.response;

      switch (status) {
        case 401:
          return this.handleUnauthorizedError(data);
        case 403:
          return this.handleForbiddenError(data);
        case 404:
          return this.handleNotFoundError(data);
        case 500:
          return this.handleServerError(data);
        default:
          return this.handleBadRequestError(data);
      }
    } else if (
      error.code === 'ECONNABORTED' ||
      error.message?.includes('timeout')
    ) {
      return {
        code: ERROR_CODES.TIMEOUT_ERROR,
        message: this.getErrorMessage(ERROR_CODES.TIMEOUT_ERROR),
      };
    } else if (error.message?.includes('Network Error')) {
      return {
        code: ERROR_CODES.NETWORK_ERROR,
        message: this.getErrorMessage(ERROR_CODES.NETWORK_ERROR),
      };
    } else {
      return {
        code: ERROR_CODES.UNKNOWN_ERROR,
        message: this.getErrorMessage(ERROR_CODES.UNKNOWN_ERROR),
      };
    }
  }

  // 处理401错误
  static handleUnauthorizedError(data) {
    return {
      code: ERROR_CODES.UNAUTHORIZED,
      message: data?.message || this.getErrorMessage(ERROR_CODES.UNAUTHORIZED),
    };
  }

  // 处理403错误
  static handleForbiddenError(data) {
    return {
      code: ERROR_CODES.FORBIDDEN,
      message: data?.message || this.getErrorMessage(ERROR_CODES.FORBIDDEN),
    };
  }

  // 处理404错误
  static handleNotFoundError(data) {
    return {
      code: ERROR_CODES.NOT_FOUND,
      message: data?.message || this.getErrorMessage(ERROR_CODES.NOT_FOUND),
    };
  }

  // 处理500错误
  static handleServerError(data) {
    return {
      code: ERROR_CODES.SYSTEM_ERROR,
      message: data?.message || this.getErrorMessage(ERROR_CODES.SYSTEM_ERROR),
    };
  }

  // 处理400错误
  static handleBadRequestError(data) {
    return {
      code: ERROR_CODES.BAD_REQUEST,
      message: data?.message || this.getErrorMessage(ERROR_CODES.BAD_REQUEST),
    };
  }

  // 显示错误信息
  static showErrorMessage(error) {
    const errorInfo = this.handleHttpError(error);

    // 这里可以集成Element Plus的消息提示
    if (typeof import.meta.env !== 'undefined' && import.meta.env.DEV) {
      console.error('API Error:', errorInfo);
    }

    return errorInfo;
  }

  // 清除认证存储
  static clearAuthStorage() {
    // 清除cookie
    document.cookie =
      'user_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    document.cookie =
      'user_user=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    document.cookie =
      'user_refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
  }
}

export default ErrorHandler;
