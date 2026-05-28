package com.idleitems.school.config;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.common.Result;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.PessimisticLockException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常处理
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletResponse response) {
        if (e.getHttpStatus() > 0) {
            response.setStatus(e.getHttpStatus());
        }
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    // 参数异常处理
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数异常: {}", e.getMessage());
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    // 约束校验异常处理（@Validated 触发的参数校验失败）
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleConstraintViolationException(ConstraintViolationException e) {
        Map<String, String> errors = new HashMap<>();
        e.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(propertyPath, message);
        });
        log.warn("参数校验失败: {}", errors);
        return Result.error("参数校验失败", errors);
    }

    // 安全异常处理
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleSecurityException(SecurityException e) {
        log.warn("安全异常: {}", e.getMessage());
        return Result.error(ErrorCode.FORBIDDEN.getCode(), e.getMessage());
    }

    // 文件上传异常处理
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("文件大小超出限制: {}", e.getMessage());
        return Result.error(ErrorCode.FILE_SIZE_EXCEEDED.getCode(), "文件大小不能超过5MB");
    }

    // IO异常处理
    @ExceptionHandler(java.io.IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleIOException(java.io.IOException e) {
        log.error("IO异常: {}", e.getMessage());
        return Result.error(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "文件处理失败，请检查文件是否损坏");
    }

    // 参数校验异常处理
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("参数校验失败: {}", errors);
        return Result.error("参数校验失败", errors);
    }

    // Spring Boot 3.x 参数校验异常处理
    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        Map<String, String> errors = new HashMap<>();
        // 提取参数校验错误信息
        String message = e.getMessage();
        if (message != null && message.contains("=")) {
            // 尝试解析类似 {rating=评分不能为空} 的格式
            String[] parts = message.split("[{}]");
            for (String part : parts) {
                if (part.contains("=")) {
                    String[] keyValue = part.split("=", 2);
                    if (keyValue.length == 2) {
                        errors.put(keyValue[0].trim(), keyValue[1].trim());
                    }
                }
            }
        }
        if (errors.isEmpty()) {
            errors.put("error", message != null ? message : "参数校验失败");
        }
        log.warn("参数校验失败: {}", errors);
        return Result.error("参数校验失败", errors);
    }

    // 空指针异常处理
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常: ", e);
        return Result.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "系统内部错误，请稍后重试");
    }

    // 数据库锁超时异常处理
    @ExceptionHandler(PessimisticLockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Void> handlePessimisticLockException(PessimisticLockException e) {
        log.warn("数据库锁等待超时，操作冲突: {}", e.getMessage());
        return Result.error(ErrorCode.CONFLICT.getCode(), "当前操作人数较多，请稍后重试");
    }

    // 运行时异常处理
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);
        return Result.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "系统内部错误，请稍后重试");
    }

    // JWT异常处理
    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleJwtException(JwtException e) {
        log.warn("JWT异常: {}", e.getMessage());
        return Result.error(ErrorCode.UNAUTHORIZED.getCode(), "Token无效或已过期");
    }
    
    // 访问被拒绝异常处理
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("访问被拒绝: {}", e.getMessage());
        return Result.error(ErrorCode.FORBIDDEN.getCode(), "无权访问");
    }
    
    // 非法状态异常处理
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalStateException(IllegalStateException e) {
        log.warn("非法状态: {}", e.getMessage());
        return Result.error(ErrorCode.OPERATION_NOT_ALLOWED.getCode(), e.getMessage());
    }

    // 数据完整性违反异常处理
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Void> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("数据完整性违反: {}", e.getMessage());
        return Result.error(ErrorCode.CONFLICT.getCode(), "数据冲突，请稍后重试");
    }
    
    // 请求路径不存在异常处理
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoHandlerFound(NoHandlerFoundException e) {
        return Result.error(ErrorCode.NOT_FOUND.getCode(), "资源不存在");
    }
    
    // 请求方法不允许异常处理
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        return Result.error(405, "请求方法不允许");
    }
    
    // JSON解析异常处理
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), "请求体格式错误");
    }

    // 静态资源未找到异常处理
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("静态资源未找到: {}", e.getMessage());
        return Result.error(ErrorCode.NOT_FOUND.getCode(), "资源不存在");
    }

    // 系统异常处理
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "系统繁忙，请稍后重试");
    }
}
