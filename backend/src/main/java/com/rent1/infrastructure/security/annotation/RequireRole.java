package com.rent1.infrastructure.security.annotation;

import com.rent1.domain.common.MemberType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口级权限声明 - 基于角色的权限控制
 * <p>
 * 标注在 Controller 方法或类上，声明当前接口允许哪些角色访问。
 * 未标注的接口默认要求登录（由 {@link AuthInterceptor} 统一处理）。
 * </p>
 * 使用示例：
 * <pre>{@code
 * // 仅房东可访问
 * @RequireRole({MemberType.LANDLORD)
 * @GetMapping("/property")
 * public ApiResponse<...> landlordOnly();
 *
 * // 仅管理员可访问
 * @RequireRole(MemberType.ADMIN)
 * @GetMapping("/admin/settings")
 * public ApiResponse<...> adminOnly();
 * }</pre>
 * 对应任务：SEC-020 角色权限校验拦截器
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    /** 允许访问的角色列表 */
    MemberType[] value();
}
