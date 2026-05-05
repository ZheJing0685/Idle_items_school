package com.idleitems.school.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    SUCCESS(200, "操作成功", HttpStatus.OK),
    CREATED(201, "创建成功", HttpStatus.CREATED),

    BAD_REQUEST(400, "请求参数错误", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(401, "未授权，请先登录", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403, "无权限访问", HttpStatus.FORBIDDEN),
    NOT_FOUND(404, "资源不存在", HttpStatus.NOT_FOUND),
    CONFLICT(409, "资源冲突", HttpStatus.CONFLICT),

    VALIDATION_ERROR(400, "数据校验失败", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(400, "密码格式不正确", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(401, "Token无效或已过期", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(401, "Token已过期", HttpStatus.UNAUTHORIZED),

    USER_NOT_FOUND(404, "用户不存在", HttpStatus.NOT_FOUND),
    ITEM_NOT_FOUND(404, "物品不存在", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND(404, "订单不存在", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(404, "分类不存在", HttpStatus.NOT_FOUND),

    USER_ALREADY_EXISTS(409, "用户已存在", HttpStatus.CONFLICT),
    ITEM_ALREADY_EXISTS(409, "物品已存在", HttpStatus.CONFLICT),

    INSUFFICIENT_PERMISSION(403, "权限不足", HttpStatus.FORBIDDEN),
    OPERATION_NOT_ALLOWED(403, "当前状态不允许此操作", HttpStatus.FORBIDDEN),

    FILE_UPLOAD_ERROR(500, "文件上传失败", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_TYPE_NOT_ALLOWED(400, "不支持的文件类型", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED(400, "文件大小超过限制", HttpStatus.BAD_REQUEST),

    INTERNAL_SERVER_ERROR(500, "服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(503, "服务暂不可用", HttpStatus.SERVICE_UNAVAILABLE);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
