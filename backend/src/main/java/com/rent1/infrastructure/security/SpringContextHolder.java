package com.rent1.infrastructure.security;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 为非 Spring 管理的类（如 MyBatis TypeHandler）提供访问 Spring Bean 的能力。
 * <p>
 * 设计背景：MyBatis TypeHandler 的实例化由 MyBatis 框架控制，
 * 无法直接通过 @Autowired 注入 CryptoService。此工具类通过
 * ApplicationContextAware 方式持有容器引用，供 TypeHandler 静态访问。
 * </p>
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> beanType) {
        if (context == null) {
            throw new IllegalStateException("Spring context 尚未初始化");
        }
        return context.getBean(beanType);
    }
}
