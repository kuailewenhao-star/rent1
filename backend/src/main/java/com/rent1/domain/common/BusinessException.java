package com.rent1.domain.common;

import com.rent1.common.enums.ErrorCode;
import lombok.Getter;

/**
 * 业务异常基类
 *
 * 用于封装业务规则校验失败、权限校验失败等场景
 * 所有异常场景必须捕获并处理，禁止抛出原始异常堆栈给前端
 *
 * 全局异常处理器 {@code com.rent1.common.exception.GlobalExceptionHandler}
 * 捕获此类异常并转换为统一响应格式。
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误消息
     */
    private final String message;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
        this.message = customMessage;
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}
