package com.rent1.infrastructure.security;

import com.rent1.domain.common.BusinessException;
import com.rent1.infrastructure.security.annotation.RequestIntegrity;
import com.rent1.infrastructure.cache.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * 防重放攻击 + 请求签名校验拦截器（基础设施层）
 * <p>
 * 当 Controller 方法或类上标注 {@link RequestIntegrity} 时：
 * <ol>
 *     <li>校验 X-Timestamp 与服务器时间差 ≤ 未超过注解声明的时间窗口</li>
 *     <li>在 Redis 中记录 X-Nonce，同一 nonce 在时间窗口内仅允许一次请求</li>
 *     <li>若 {@link RequestIntegrity#requireSignature()} 为 true，则校验请求头
 *         需携带 X-Signature = HMAC-SHA256(secretKey, body + "|" + timestamp + "|" + nonce)</li>
 * </ol>
 * </p>
 * 对应任务：
 * <ul>
 *     <li>SEC-030 防重放攻击验证</li>
 *     <li>SEC-031 请求参数签名防篡改</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestIntegrityInterceptor implements HandlerInterceptor {

    public static final String HEADER_TIMESTAMP = "X-Timestamp";
    public static final String HEADER_NONCE = "X-Nonce";
    public static final String HEADER_SIGNATURE = "X-Signature";

    private final RedisService redisService;

    @Value("${request.security.signature-secret:rent1-default-signature-secret}")
    private String signatureSecret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod hm = (HandlerMethod) handler;
        RequestIntegrity ann = hm.getMethodAnnotation(RequestIntegrity.class);
        if (ann == null) {
            ann = AnnotationUtils.findAnnotation(hm.getBeanType(), RequestIntegrity.class);
        }
        if (ann == null) {
            return true;
        }

        // 1. 时间戳校验
        String timestampStr = request.getHeader(HEADER_TIMESTAMP);
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException ex) {
            throw new BusinessException("S001", "请求时间戳无效");
        }
        long diff = Math.abs(System.currentTimeMillis() - timestamp);
        if (diff > ann.timeWindowMs()) {
            throw new BusinessException("S001", "请求已过期");
        }

        // 2. nonce 校验（幂等性
        String nonce = request.getHeader(HEADER_NONCE);
        if (nonce == null || nonce.length() < 8) {
            throw new BusinessException("S002", "请求已被使用");
        }
        String nonceKey = "nonce:" + nonce;
        if (Boolean.TRUE.equals(redisService.exists(nonceKey))) {
            throw new BusinessException("S002", "请求已被使用");
        }
        redisService.set(nonceKey, "1", ann.nonceTtlSec(), java.util.concurrent.TimeUnit.SECONDS);

        // 3. 签名校验（若要求开启）
        if (ann.requireSignature()) {
            String signature = request.getHeader(HEADER_SIGNATURE);
            if (signature == null || signature.isEmpty()) {
                throw new BusinessException("S010", "请求参数被篡改");
            }
            String body;
            try {
                body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new BusinessException("S010", "请求参数被篡改");
            }
            String expected = HmacUtils.hmacSha256Hex(signatureSecret,
                    (body == null ? "" : body) + "|" + timestampStr + "|" + nonce);
            if (!expected.equalsIgnoreCase(signature)) {
                log.warn("Signature mismatch: expected={}, actual={}", expected, signature);
                throw new BusinessException("S010", "请求参数被篡改");
            }
        }
        return true;
    }
}
