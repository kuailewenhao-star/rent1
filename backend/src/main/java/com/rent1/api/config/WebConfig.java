package com.rent1.api.config;

import com.rent1.infrastructure.security.EncryptedApiInterceptor;
import com.rent1.infrastructure.security.IdempotentInterceptor;
import com.rent1.infrastructure.security.RequestIntegrityInterceptor;
import com.rent1.api.interceptor.AuthInterceptor;
import com.rent1.api.interceptor.PermissionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web/MVC 配置 - 注册拦截器链
 * <p>
 * 执行顺序：
 * <ol>
 *     <li>{@link RequestIntegrityInterceptor} - 防重放 + 签名校验</li>
 *     <li>{@link AuthInterceptor} - 登录态校验（JWT）</li>
 *     <li>{@link PermissionInterceptor} - 角色权限校验</li>
 *     <li>{@link IdempotentInterceptor} - 关键操作幂等防重复提交</li>
 *     <li>{@link EncryptedApiInterceptor} - 敏感接口传输加密标记</li>
 * </ol>
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final PermissionInterceptor permissionInterceptor;
    private final RequestIntegrityInterceptor requestIntegrityInterceptor;
    private final IdempotentInterceptor idempotentInterceptor;
    private final EncryptedApiInterceptor encryptedApiInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 1. 防重放与签名校验（最先执行；匿名接口也可开启）
        registry.addInterceptor(requestIntegrityInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/health", "/error");

        // 2. 登录态校验
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/wechat/login",
                        "/admin/auth/login",
                        "/health",
                        "/error"
                );

        // 3. 角色权限校验（需在登录态之后执行）
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**",
                        "/admin/auth/**",
                        "/health",
                        "/error"
                );

        // 4. 幂等防重复提交（需在登录态之后拿到 memberId）
        registry.addInterceptor(idempotentInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**",
                        "/admin/auth/**",
                        "/health",
                        "/error"
                );

        // 5. 敏感接口传输加密（对 body 做加密/解密标记，具体加解密由 advice 完成）
        registry.addInterceptor(encryptedApiInterceptor)
                .addPathPatterns("/**");
    }
}
