package com.rent1.api.interceptor;

import com.rent1.domain.common.BusinessException;
import com.rent1.common.enums.ErrorCode;
import com.rent1.infrastructure.security.DataScopeContext;
import com.rent1.infrastructure.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录态校验拦截器（接入层 - 无业务逻辑）
 * <p>
 * 职责：
 * <ol>
 *     <li>从请求头 Authorization: Bearer &lt;token&gt; 解析 JWT</li>
 *     <li>校验 token 有效性与过期状态</li>
 *     <li>将 memberId、memberType、subjectId 写入请求属性 +
 *         {@link DataScopeContext}（供 Repository 层的数据权限过滤使用）</li>
 * </ol>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    public static final String HEADER_TOKEN = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String ATTR_MEMBER_ID = "memberId";
    public static final String ATTR_MEMBER_TYPE = "memberType";
    public static final String ATTR_SUBJECT_ID = "subjectId";

    private final JwtService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = extractToken(request);
        if (token == null) {
            throw new BusinessException(ErrorCode.A004);
        }

        try {
            Claims claims = jwtService.parseToken(token);
            String memberId = (String) claims.get("memberId");
            String memberType = (String) claims.get("memberType");
            String subjectId = (String) claims.get("subjectId");
            request.setAttribute(ATTR_MEMBER_ID, memberId);
            request.setAttribute(ATTR_MEMBER_TYPE, memberType);
            request.setAttribute(ATTR_SUBJECT_ID, subjectId);

            // 将当前登录身份写入 ThreadLocal，供 Repository/Service 层
            // 数据权限过滤使用（landlord_member_id = current_member_id）
            DataScopeContext.set(memberId, memberType);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
            throw new BusinessException("A005", "登录已过期，请重新登录");
        } catch (JwtException e) {
            log.warn("Invalid token: {}", e.getMessage());
            throw new BusinessException(ErrorCode.A001);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清理 ThreadLocal，避免线程复用导致数据污染
        DataScopeContext.clear();
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER_TOKEN);
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
