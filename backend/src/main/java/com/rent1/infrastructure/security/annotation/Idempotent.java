package com.rent1.infrastructure.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 关键操作幂等性声明
 * <p>
 * 标注在 Controller 方法上，配合 Redis 分布式锁实现"同一操作短时间内仅执行一次"。
 * 典型应用场景：
 * <ul>
 *     <li>合约创建：避免快速点击产生多个相同合约</li>
 *     <li>账单核销：避免重复扣款</li>
 *     <li>押金退还：避免重复退还</li>
 *     <li>微信登录：避免并发条件下重复注册</li>
 * </ul>
 * 键生成规则：
 * <pre>idempotent:请求URI:memberId:keyExpr</pre>
 * </p>
 * 对应任务：SEC-040 关键操作防重复提交
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * 锁的过期时间（默认 30 秒），即同一条请求在 30 秒内仅允许执行一次。
     */
    long ttl() default 30L;

    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 可选：从请求体/请求参数中提取 key（Spring SpEL 风格）。
     * 留空时使用 memberId + URI 作为默认锁粒度。
     * 示例："#body.contractId" 表示从请求体中读取 contractId 字段。
     */
    String keyExpr() default "";
}
