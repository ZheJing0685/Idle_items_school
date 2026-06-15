package com.idleitems.school.common.aspect;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.common.annotation.Idempotent;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotentAspectTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private IdempotentAspect idempotentAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private HttpServletRequest request;

    private ServletRequestAttributes attributes;

    @BeforeEach
    void setUp() throws Exception {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(IdempotentAspectTest.class.getMethod("dummyMethod"));

        attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Idempotent(message = "请勿重复提交")
    public void dummyMethod() {
    }

    @Test
    void checkIdempotency_FirstRequest_Proceeds() throws Throwable {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(request.getHeader("Idempotency-Key")).thenReturn("unique-key-123");
        when(valueOperations.setIfAbsent("idempotent:unique-key-123", "processed", 86400, TimeUnit.SECONDS))
                .thenReturn(true);
        when(joinPoint.proceed()).thenReturn("success");

        Object result = idempotentAspect.checkIdempotency(joinPoint);

        assertEquals("success", result);
        verify(valueOperations, times(1)).setIfAbsent(anyString(), anyString(), anyLong(), any());
        verify(joinPoint, times(1)).proceed();
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void checkIdempotency_DuplicateRequest_ThrowsBusinessException() throws Throwable {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(request.getHeader("Idempotency-Key")).thenReturn("duplicate-key");
        when(valueOperations.setIfAbsent("idempotent:duplicate-key", "processed", 86400, TimeUnit.SECONDS))
                .thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> idempotentAspect.checkIdempotency(joinPoint));
        assertEquals(ErrorCode.CONFLICT.getCode(), ex.getCode());
        assertEquals("请勿重复提交", ex.getMessage());
        verify(joinPoint, never()).proceed();
    }

    @Test
    void checkIdempotency_BusinessException_RemovesKey() throws Throwable {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(request.getHeader("Idempotency-Key")).thenReturn("retry-key");
        when(valueOperations.setIfAbsent("idempotent:retry-key", "processed", 86400, TimeUnit.SECONDS))
                .thenReturn(true);
        when(joinPoint.proceed()).thenThrow(new BusinessException(ErrorCode.BAD_REQUEST, "业务异常"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> idempotentAspect.checkIdempotency(joinPoint));
        assertEquals("业务异常", ex.getMessage());
        verify(redisTemplate, times(1)).delete("idempotent:retry-key");
    }

    @Test
    void checkIdempotency_OtherException_RemovesKey() throws Throwable {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(request.getHeader("Idempotency-Key")).thenReturn("error-key");
        when(valueOperations.setIfAbsent("idempotent:error-key", "processed", 86400, TimeUnit.SECONDS))
                .thenReturn(true);
        when(joinPoint.proceed()).thenThrow(new RuntimeException("系统异常"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> idempotentAspect.checkIdempotency(joinPoint));
        assertEquals("系统异常", ex.getMessage());
        verify(redisTemplate, times(1)).delete("idempotent:error-key");
    }

    @Test
    void checkIdempotency_NoIdempotencyKey_Proceeds() throws Throwable {
        when(request.getHeader("Idempotency-Key")).thenReturn(null);
        when(joinPoint.proceed()).thenReturn("no-key-result");

        Object result = idempotentAspect.checkIdempotency(joinPoint);

        assertEquals("no-key-result", result);
        verify(valueOperations, never()).setIfAbsent(anyString(), anyString(), anyLong(), any());
        verify(joinPoint, times(1)).proceed();
    }
}
