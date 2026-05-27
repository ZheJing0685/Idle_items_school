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
} as const;

export type ErrorCodeValue = typeof ErrorCode[keyof typeof ErrorCode]

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
} as const;

export type ErrorTypeValue = typeof ErrorType[keyof typeof ErrorType]

interface AxiosErrorLike {
  response?: {
    status?: number
    data?: { code?: number }
  }
  code?: string
  message?: string
}

export function classifyError(error: any): ErrorTypeValue {
  if (!error) return ErrorType.UNKNOWN_ERROR;

  const err: AxiosErrorLike = error;
  const status = err.response?.status;
  const code = err.code;

  if (code === 'ECONNABORTED' || err.message?.includes('timeout')) {
    return ErrorType.TIMEOUT_ERROR;
  }

  if (code === 'ERR_NETWORK' || err.message?.includes('Network Error')) {
    return ErrorType.NETWORK_ERROR;
  }

  if (status === 401) return ErrorType.AUTHENTICATION_ERROR;
  if (status === 403) return ErrorType.AUTHORIZATION_ERROR;
  if (status === 404) return ErrorType.NOT_FOUND_ERROR;
  if (status === 409) return ErrorType.CONFLICT_ERROR;
  if (status === 500) return ErrorType.SERVER_ERROR;

  if (status != null && status >= 400 && status < 500) {
    return ErrorType.CLIENT_ERROR;
  }

  const backendCode = err.response?.data?.code;
  if (backendCode === 401) return ErrorType.AUTHENTICATION_ERROR;
  if (backendCode === 403) return ErrorType.AUTHORIZATION_ERROR;
  if (backendCode === 404) return ErrorType.NOT_FOUND_ERROR;
  if (backendCode != null && backendCode >= 500) return ErrorType.SERVER_ERROR;

  return ErrorType.UNKNOWN_ERROR;
}

export default {
  ErrorCode,
  ErrorType,
  classifyError,
};
