package com.idleitems.school.common.aspect;

import com.idleitems.school.common.annotation.Timeout;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.*;

@Slf4j
@Aspect
@Component
public class TimeoutAspect {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Around("@annotation(com.idleitems.school.common.annotation.Timeout)")
    public Object handleTimeout(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Timeout timeout = method.getAnnotation(Timeout.class);

        long timeoutValue = timeout.value();
        TimeUnit unit = timeout.unit();

        Future<Object> future = executor.submit(() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new RuntimeException(e);
            }
        });

        try {
            return future.get(timeoutValue, unit);
        } catch (TimeoutException e) {
            future.cancel(true);
            log.warn("方法 {} 执行超时: {} {}", method.getName(), timeoutValue, unit);
            throw new RuntimeException("操作超时，请稍后重试");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause);
        }
    }
}
