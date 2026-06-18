package com.rent1.infrastructure.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 敏感接口传输加密标记注解。
 * <p>
 * 标注在 Controller 方法或类上，表示：
 * <ul>
 *     <li>入参（RequestBody）需要解密后才能被业务方法消费</li>
 *     <li>返回值（ResponseBody）需要加密后再返回前端</li>
 * </ul>
 * 使用方式：
 * <pre>{@code
 * @PostMapping("/login")
 * @EncryptedApi
 * public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request);
 * }</pre>
 * 对应任务：SEC-002 敏感接口数据传输整体加密
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EncryptedApi {
}
