package com.idleitems.school.common.annotation;

import java.lang.annotation.*;

/**
 * 接口幂等性注解
 * 标注在 Controller 方法上，防止客户端重复提交导致重复下单或重复支付。
 * <p>
 * 客户端需在请求头中传入 {@code Idempotency-Key}（UUID），
 * 服务端通过 Redis 记录该 key 并在过期时间内拒绝重复请求。
 * <p>
 * 使用示例：
 * <pre>{@code
 * @Idempotent(message = "订单正在处理中，请勿重复提交")
 * @PostMapping
 * public Result<OrderSummaryResponse> createOrder(...) { ... }
 * }</pre>
 *
 * @see com.idleitems.school.common.aspect.IdempotentAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 幂等 key 的来源，支持 SpEL 表达式。
     * 为空时默认从请求头 {@code Idempotency-Key} 获取。
     */
    String key() default "";

    /**
     * 幂等 key 在 Redis 中的过期时间（秒），默认 86400（24 小时）。
     * 过期后允许使用相同的 key 再次提交。
     */
    int ttl() default 86400;

    /**
     * 重复提交时返回给客户端的提示消息。
     */
    String message() default "请求正在处理中，请勿重复提交";
}
