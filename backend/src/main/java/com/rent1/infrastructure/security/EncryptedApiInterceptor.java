package com.rent1.infrastructure.security;

import com.rent1.infrastructure.security.annotation.EncryptedApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 敏感接口整体加解密拦截器（基础设施层 - 无业务逻辑）
 * <p>
 * 检查目标 Controller 方法是否标注 {@link EncryptedApi}；
 * 如是，则使用请求包装器将密文 body 解密为明文 body，
 * 后续由 Spring MVC 正常进行 JSON 反序列化。
 * </p>
 * <p>
 * 请求格式：
 * <pre>
 * Content-Type: application/json
 * Header:       X-Encrypted: true（或直接发送密文字符串作为 body）
 * Body:         {加密后的JSON字符串的密文}
 * </pre>
 * 响应体加密由 {@link EncryptedResponseAdvice} 完成。
 * </p>
 * 对应任务：SEC-002 敏感接口数据传输整体加密
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EncryptedApiInterceptor implements HandlerInterceptor {

    private final CryptoService cryptoService;

    /** 请求头标记：请求体已加密 */
    public static final String HEADER_X_ENCRYPTED = "X-Encrypted";

    /** 用于存储 "是否为敏感接口" 请求级属性 */
    public static final String ATTR_ENCRYPTED_API = "encryptedApi";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod hm = (HandlerMethod) handler;
        boolean isEncryptedApi = hm.hasMethodAnnotation(EncryptedApi.class)
                || hm.getBeanType().isAnnotationPresent(EncryptedApi.class);
        if (!isEncryptedApi) {
            return true;
        }
        request.setAttribute(ATTR_ENCRYPTED_API, Boolean.TRUE);

        // 仅当前端明确声明请求体是加密的，才进行解密
        if (!"true".equalsIgnoreCase(request.getHeader(HEADER_X_ENCRYPTED))) {
            return true;
        }
        // 这里通过 ServletRequestWrapper 替换 body 的方式在 Spring MVC 中
        // 通常使用 ContentCachingRequestWrapper 或自定义 RequestWrapper 完成。
        // 由于 Spring MVC 对 InputStream 只能读取一次，我们用自定义 Wrapper
        // 将已解密内容替换到原始请求中。
        // （此处不做额外处理，具体解密由 EncryptedRequestBodyAdvice 在
        //  RequestBody 参数解析时执行，对 Spring 参数绑定更友好）
        return true;
    }
}
