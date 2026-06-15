package com.idleitems.school.common.aspect;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.common.annotation.Idempotent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性校验切面。
 * <p>
 * 拦截所有标注了 {@link Idempotent @Idempotent} 的 Controller 方法，
 * 从请求头 {@code Idempotency-Key} 获取幂等 key，利用 Redis SETNX 实现去重：
 * <ul>
 *   <li>首次请求：SETNX 成功，放行执行业务逻辑</li>
 *   <li>重复请求：SETNX 失败（key 已存在），抛出 {@link BusinessException}（409 Conflict）</li>
 *   <li>业务异常：切面自动删除 Redis key，允许客户端重试</li>
 * </ul>
 * <p>
 * 设计上与 {@link com.idleitems.school.common.aspect.PermissionAspect} 保持一致的 AOP 模式。
 *
 * @see Idempotent
 * @see com.idleitems.school.common.aspect.PermissionAspect
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {

    /**
     * Redis 中幂等 key 的统一前缀，用于与其它 Redis key 隔离。
     */
    private static final String IDEMPOTENT_KEY_PREFIX = "idempotent:";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 环绕通知：在方法执行前校验幂等性，方法执行异常时自动释放幂等 key。
     */
    @Around("@annotation(com.idleitems.school.common.annotation.Idempotent)")
    public Object checkIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Idempotent idempotent = method.getAnnotation(Idempotent.class);

        // 从请求头获取幂等 key
        String idempotencyKey = getIdempotencyKeyFromHeader();
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            // 如果没有传幂等 key，放行（兼容旧客户端）
            return joinPoint.proceed();
        }

        String redisKey = IDEMPOTENT_KEY_PREFIX + idempotencyKey;

        // 尝试设置幂等 key（SETNX），如果已存在则拒绝重复请求
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "processed", idempotent.ttl(), TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(success)) {
            log.warn("检测到重复请求，idempotencyKey: {}", idempotencyKey);
            throw new BusinessException(ErrorCode.CONFLICT.getCode(), idempotent.message());
        }

        try {
            return joinPoint.proceed();
        } catch (BusinessException e) {
            // 业务异常时删除幂等 key，允许客户端使用相同 key 重试
            log.info("业务处理失败，清除幂等 key 以允许重试，idempotencyKey: {}", idempotencyKey);
            redisTemplate.delete(redisKey);
            throw e;
        } catch (Exception e) {
            // 系统异常时也删除幂等 key，避免 key 残留导致用户无法重试
            log.warn("系统异常，清除幂等 key 以允许重试，idempotencyKey: {}", idempotencyKey, e);
            redisTemplate.delete(redisKey);
            throw e;
        }
    }

    /**
     * 从当前 HTTP 请求头中获取 {@code Idempotency-Key}。
     *
     * @return 幂等 key 值，如果无法获取请求上下文则返回 {@code null}
     */
    private String getIdempotencyKeyFromHeader() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader("Idempotency-Key");
        }
        return null;
    }
}
