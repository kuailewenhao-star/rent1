package com.rent1.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 统一响应封装
 * 
 * 所有接口响应统一通过此类封装
 * 禁止包含业务规则，仅做响应格式化
 * 
 * 响应格式：
 * {
 *   "code": "0000",      // 成功码
 *   "message": "success", // 提示消息
 *   "data": {},          // 业务数据
 *   "timestamp": 123456789 // 时间戳
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    /**
     * 成功码
     */
    public static final String SUCCESS_CODE = "0000";
    
    /**
     * 成功消息
     */
    public static final String SUCCESS_MESSAGE = "success";
    
    /**
     * 响应码
     */
    private String code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 业务数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .code(SUCCESS_CODE)
                .message(SUCCESS_MESSAGE)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 成功响应（有数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(SUCCESS_CODE)
                .message(SUCCESS_MESSAGE)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code(SUCCESS_CODE)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 失败响应（错误码）
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 失败响应（错误码枚举）
     */
    public static <T> ApiResponse<T> error(com.rent1.common.enums.ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return SUCCESS_CODE.equals(code);
    }
}