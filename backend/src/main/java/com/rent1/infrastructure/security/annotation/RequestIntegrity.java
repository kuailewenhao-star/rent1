package com.rent1.infrastructure.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 接口防重放 & 签名校验声明注解（基础安全防护）
 * <p>
 * 标注在 Controller 方法或类上：
 * <ol>
 *     <li>校验请求头 {@code X-Timestamp} 与服务器时间差不超过配置阈值（默认 5 分钟）</li>
 *     <li>校验请求头 {@code X-Nonce} 在 Redis 中不存在（5 分钟窗口内唯一）</li>
 *     <li>若 {@link #requireSignature()} 为 true，则校验 {@code X-Signature}
 *         与后端重新计算的签名一致（签名规则 = HMAC(requestBody + timestamp + nonce, secretKey)）</li>
 * </ol>
 * </p>
 * 对应任务：
 * <ul>
 *     <li>SEC-030 防重放攻击验证</li>
 *     <li>SEC-031 请求参数签名防篡改</li>
 * </ul>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestIntegrity {

    /** 允许的请求时间与服务器时间差（毫秒），默认 5 分钟 */
    long timeWindowMs() default 5 * 60 * 1000L;

    /** nonce 在 Redis 中的过期时间（秒），默认 5 分钟 */
    long nonceTtlSec() default 5 * 60L;

    /** 是否强制校验签名（默认 false - 仅做时间戳 + nonce 防重放） */
    boolean requireSignature() default false;
}
