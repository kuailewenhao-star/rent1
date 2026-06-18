package com.rent1.infrastructure.security;

import com.rent1.infrastructure.security.annotation.EncryptedApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * 对标注 {@link EncryptedApi} 的接口做请求体解密。
 * <p>
 * 当前端通过 {@code X-Encrypted: true} 头表明请求体是密文时，
 * 该 Advice 在参数绑定时将密文 body 解密为明文，再交给 Jackson 反序列化。
 * </p>
 * 对应任务：SEC-002 敏感接口数据传输整体加密
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class EncryptedRequestBodyAdvice implements RequestBodyAdvice {

    private final CryptoService cryptoService;

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.hasMethodAnnotation(EncryptedApi.class)
                || (methodParameter.getContainingClass() != null
                && methodParameter.getContainingClass().isAnnotationPresent(EncryptedApi.class));
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
                                           Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        String header = inputMessage.getHeaders().getFirst(EncryptedApiInterceptor.HEADER_X_ENCRYPTED);
        if (!"true".equalsIgnoreCase(header)) {
            return inputMessage;
        }
        byte[] encryptedBytes = inputMessage.getBody().readAllBytes();
        if (encryptedBytes.length == 0) {
            return inputMessage;
        }
        String encryptedText = new String(encryptedBytes, StandardCharsets.UTF_8).trim();
        // 去除前后引号（前端有时候会以 JSON 字符串形式发送）
        if (encryptedText.length() > 1 && encryptedText.startsWith("\"") && encryptedText.endsWith("\"")) {
            encryptedText = encryptedText.substring(1, encryptedText.length() - 1);
        }
        String plainText;
        try {
            plainText = cryptoService.decrypt(encryptedText);
        } catch (Exception e) {
            log.warn("请求体解密失败，按明文处理", e);
            plainText = encryptedText;
        }
        final byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(plainBytes);
            }

            @Override
            public HttpHeaders getHeaders() {
                return inputMessage.getHeaders();
            }
        };
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                  Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }
}
