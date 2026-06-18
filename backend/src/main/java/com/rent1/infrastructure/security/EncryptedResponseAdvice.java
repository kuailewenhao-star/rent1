package com.rent1.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rent1.infrastructure.security.annotation.EncryptedApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 对标注 {@link EncryptedApi} 的接口响应体做整体加密。
 * <p>
 * 当请求带有 {@code X-Encrypted: true} 头时，
 * 将业务方法返回的 JSON 对象整体加密为字符串返回。
 * </p>
 * 对应任务：SEC-002 敏感接口数据传输整体加密
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class EncryptedResponseAdvice implements ResponseBodyAdvice<Object> {

    private final CryptoService cryptoService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (returnType.getMethod() == null) {
            return false;
        }
        return returnType.hasMethodAnnotation(EncryptedApi.class)
                || returnType.getContainingClass().isAnnotationPresent(EncryptedApi.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        String header = request.getHeaders().getFirst(EncryptedApiInterceptor.HEADER_X_ENCRYPTED);
        if (!"true".equalsIgnoreCase(header)) {
            return body;
        }
        try {
            String json = objectMapper.writeValueAsString(body);
            return cryptoService.encrypt(json);
        } catch (Exception e) {
            log.error("响应体加密失败", e);
            return body;
        }
    }
}
