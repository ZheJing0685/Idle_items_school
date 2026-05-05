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

  if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
    return ErrorType.TIMEOUT_ERROR;
  }

  if (error.message?.includes('Network Error')) {
    return ErrorType.NETWORK_ERROR;
  }

  if (error.code === 401) {
    return ErrorType.AUTHENTICATION_ERROR;
  }

  if (error.code === 403) {
    return ErrorType.AUTHORIZATION_ERROR;
  }

  if (error.code === 404) {
    return ErrorType.NOT_FOUND_ERROR;
  }

  if (error.code === 409) {
    return ErrorType.CONFLICT_ERROR;
  }

  if (error.code === 500) {
    return ErrorType.SERVER_ERROR;
  }

  if (error.code >= 400 && error.code < 500) {
    return ErrorType.CLIENT_ERROR;
  }

  return ErrorType.UNKNOWN_ERROR;
}

export default {
  ErrorCode,
  ErrorType,
  classifyError
};
