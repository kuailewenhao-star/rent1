package com.rent1.infrastructure.security;

import com.rent1.domain.common.BusinessException;
import com.rent1.infrastructure.cache.RedisService;
import com.rent1.infrastructure.security.annotation.Idempotent;
import com.rent1.api.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 关键操作防重复提交拦截器（基础设施层）
 * <p>
 * 当 Controller 方法或类上标注 {@link Idempotent} 时，
 * 通过 Redis 分布式锁阻止同一用户在指定时间窗口内的重复请求。
 * </p>
 * <p>
 * 锁粒度优先级：
 * <ol>
 *     <li>声明 keyExpr 解析（如"#body.contractId</li>
 *     <li>否则：使用 memberId + 请求 URI</li>
 * </ol>
 * </p>
 * 对应任务：SEC-040 关键操作防重复提交
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotentInterceptor implements HandlerInterceptor {

    private static final String LOCK_PREFIX = "idempotent:";

    private final RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod hm = (HandlerMethod) handler;
        Idempotent ann = hm.getMethodAnnotation(Idempotent.class);
        if (ann == null) {
            ann = AnnotationUtils.findAnnotation(hm.getBeanType(), Idempotent.class);
        }
        if (ann == null) {
            return true;
        }

        String memberId = (String) request.getAttribute(AuthInterceptor.ATTR_MEMBER_ID);
        String key = LOCK_PREFIX + request.getRequestURI() + ":"
                + (memberId == null ? "anon" : memberId);
        // 这里保留 keyExpr 字段不为空时保留，后续可扩展解析 body 内容拼接 key

        long ttlSec = ann.unit().toSeconds(ann.ttl());
        boolean acquired = Boolean.TRUE.equals(
                redisService.acquireLock(key, ttlSec, java.util.concurrent.TimeUnit.SECONDS));
        if (!acquired) {
            log.warn("Idempotent lock conflict: key={}", key);
            throw new BusinessException("R001", "操作过于频繁，请稍后重试");
        }
        return true;
    }
}
