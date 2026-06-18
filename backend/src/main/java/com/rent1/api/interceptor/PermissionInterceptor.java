package com.rent1.api.interceptor;

import com.rent1.domain.common.BusinessException;
import com.rent1.common.enums.ErrorCode;
import com.rent1.domain.common.MemberType;
import com.rent1.infrastructure.security.annotation.RequireRole;
import com.rent1.api.interceptor.AuthInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 角色权限校验拦截器（接入层 - 无业务逻辑）
 * <p>
 * 工作机制：
 * <ol>
 *     <li>读取当前请求已由 {@link AuthInterceptor} 解析出的 memberType</li>
 *     <li>检查目标 Controller 方法或类上是否标注 {@link RequireRole}</li>
 *     <li>若标注：仅当 memberType 存在于声明的角色列表中时放行</li>
 *     <li>若未标注：
 *         <ul>
 *             <li>ADMIN 接口路径仅允许 ADMIN</li>
 *             <li>租客角色禁止访问 /house-sources 等房东模块</li>
 *         </ul>
 *     </li>
 * </ol>
 * </p>
 * 对应任务：SEC-020 角色权限校验拦截器
 */
@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    /** 管理员专属路径前缀 - 仅 ADMIN 角色可访问 */
    private static final String ADMIN_PATH_PREFIX = "/admin/";

    /** 租客禁止访问的路径（房东专属资源） */
    private static final String[] TENANT_FORBIDDEN_PATHS = {
            "/house-sources",
            "/rooms",
            "/expense-invoices"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 放行预检请求
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // 1. 基于注解的细粒度权限声明
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            RequireRole methodAnn = hm.getMethodAnnotation(RequireRole.class);
            RequireRole classAnn = AnnotationUtils.findAnnotation(hm.getBeanType(), RequireRole.class);
            RequireRole effective = (methodAnn != null) ? methodAnn : classAnn;
            if (effective != null && effective.value().length > 0) {
                String memberTypeStr = (String) request.getAttribute(AuthInterceptor.ATTR_MEMBER_TYPE);
                if (memberTypeStr == null) {
                    throw new BusinessException(ErrorCode.A004);
                }
                MemberType currentType;
                try {
                    currentType = MemberType.fromCode(memberTypeStr);
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown member type: {}", memberTypeStr);
                    throw new BusinessException("P001", "无权限访问");
                }
                Set<MemberType> allowed = new HashSet<>(Arrays.asList(effective.value()));
                if (!allowed.contains(currentType)) {
                    log.warn("Permission denied: memberType={}, path={}", memberTypeStr, path);
                    throw new BusinessException("P001", "无权限访问");
                }
                return true;
            }
        }

        // 2. 管理员专属路径的兜底拦截
        if (path.contains(ADMIN_PATH_PREFIX)) {
            String memberTypeStr = (String) request.getAttribute(AuthInterceptor.ATTR_MEMBER_TYPE);
            if (memberTypeStr == null || !MemberType.ADMIN.getCode().equals(memberTypeStr)) {
                log.warn("Non-admin access to /admin/** path: memberType={}", memberTypeStr);
                throw new BusinessException("P001", "无权限访问");
            }
            return true;
        }

        // 3. 租客角色的兜底过滤（对房东专属资源的粗粒度拦截）
        String memberTypeStr = (String) request.getAttribute(AuthInterceptor.ATTR_MEMBER_TYPE);
        if (memberTypeStr != null && MemberType.TENANT.getCode().equals(memberTypeStr)) {
            for (String forbidden : TENANT_FORBIDDEN_PATHS) {
                if (path.contains(forbidden)) {
                    log.warn("Tenant attempted to access landlord-only path: {}", path);
                    throw new BusinessException("P001", "无权限访问");
                }
            }
        }
        return true;
    }
}
