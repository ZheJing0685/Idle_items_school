export const ErrorCode = {
  SUCCESS: 200,
  CREATED: 201,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  VALIDATION_ERROR: 400,
  INTERNAL_SERVER_ERROR: 500,
};

export const ErrorType = {
  SUCCESS: 'SUCCESS',
  NETWORK_ERROR: 'NETWORK_ERROR',
  TIMEOUT_ERROR: 'TIMEOUT_ERROR',
  VALIDATION_ERROR: 'VALIDATION_ERROR',
  AUTHENTICATION_ERROR: 'AUTHENTICATION_ERROR',
  AUTHORIZATION_ERROR: 'AUTHORIZATION_ERROR',
  NOT_FOUND_ERROR: 'NOT_FOUND_ERROR',
  CONFLICT_ERROR: 'CONFLICT_ERROR',
  SERVER_ERROR: 'SERVER_ERROR',
  CLIENT_ERROR: 'CLIENT_ERROR',
  UNKNOWN_ERROR: 'UNKNOWN_ERROR',
};

export function classifyError(error) {
  if (!error) {
    return ErrorType.UNKNOWN_ERROR;
  }

  // 检查axios错误响应
  const status = error.response?.status;
  const code = error.code;

  // 网络超时错误
  if (code === 'ECONNABORTED' || error.message?.includes('timeout')) {
    return ErrorType.TIMEOUT_ERROR;
  }

  // 网络连接错误
  if (code === 'ERR_NETWORK' || error.message?.includes('Network Error')) {
    return ErrorType.NETWORK_ERROR;
  }

  // HTTP状态码判断
  if (status === 401) {
    return ErrorType.AUTHENTICATION_ERROR;
  }

  if (status === 403) {
    return ErrorType.AUTHORIZATION_ERROR;
  }

  if (status === 404) {
    return ErrorType.NOT_FOUND_ERROR;
  }

  if (status === 409) {
    return ErrorType.CONFLICT_ERROR;
  }

  if (status === 500) {
    return ErrorType.SERVER_ERROR;
  }

  if (status >= 400 && status < 500) {
    return ErrorType.CLIENT_ERROR;
  }

  // 后端返回的业务错误码
  const backendCode = error.response?.data?.code;
  if (backendCode === 401) {
    return ErrorType.AUTHENTICATION_ERROR;
  }

  if (backendCode === 403) {
    return ErrorType.AUTHORIZATION_ERROR;
  }

  if (backendCode === 404) {
    return ErrorType.NOT_FOUND_ERROR;
  }

  if (backendCode >= 500) {
    return ErrorType.SERVER_ERROR;
  }

  return ErrorType.UNKNOWN_ERROR;
}

export default {
  ErrorCode,
  ErrorType,
  classifyError,
};
