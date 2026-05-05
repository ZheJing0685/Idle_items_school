package com.idleitems.school.config;

import com.idleitems.school.common.Result;
import jakarta.persistence.PessimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常处理
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    // 安全异常处理
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleSecurityException(SecurityException e) {
        log.warn("安全异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    // 文件上传异常处理
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("文件大小超出限制: {}", e.getMessage());
        return Result.error("文件大小不能超过5MB");
    }

    // IO异常处理
    @ExceptionHandler(java.io.IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleIOException(java.io.IOException e) {
        log.error("IO异常: {}", e.getMessage());
        return Result.error("文件处理失败，请检查文件是否损坏");
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

    // 空指针异常处理
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常: ", e);
        return Result.error("系统内部错误，请稍后重试");
    }

    // 数据库锁超时异常处理
    @ExceptionHandler(PessimisticLockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Void> handlePessimisticLockException(PessimisticLockException e) {
        log.warn("数据库锁等待超时，操作冲突: {}", e.getMessage());
        return Result.error("当前操作人数较多，请稍后重试");
    }

    // 运行时异常处理
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);
        return Result.error("系统内部错误，请稍后重试");
    }

    // 系统异常处理
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error("系统繁忙，请稍后重试");
    }
}
