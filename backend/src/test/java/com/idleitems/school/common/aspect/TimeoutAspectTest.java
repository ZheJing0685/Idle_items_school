package com.idleitems.school.common.aspect;

import com.idleitems.school.common.annotation.Timeout;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeoutAspectTest {

    @InjectMocks
    private TimeoutAspect timeoutAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private ExecutorService mockExecutor;
    private Future<Object> mockFuture;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(TimeoutAspectTest.class.getMethod("timedMethod"));

        mockExecutor = mock(ExecutorService.class);
        mockFuture = (Future<Object>) (Object) mock(Future.class);
        ReflectionTestUtils.setField(timeoutAspect, "executor", mockExecutor);
    }

    @Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
    public String timedMethod() {
        return "done";
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleTimeout_CompletesInTime_ReturnsResult() throws Throwable {
        when(mockExecutor.submit(any(Callable.class))).thenReturn(mockFuture);
        when(mockFuture.get(5000, TimeUnit.MILLISECONDS)).thenReturn("success");

        Object result = timeoutAspect.handleTimeout(joinPoint);

        assertEquals("success", result);
        verify(mockFuture, times(1)).get(5000, TimeUnit.MILLISECONDS);
        verify(mockFuture, never()).cancel(anyBoolean());
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleTimeout_TimesOut_ThrowsRuntimeException() throws Throwable {
        when(mockExecutor.submit(any(Callable.class))).thenReturn(mockFuture);
        when(mockFuture.get(5000, TimeUnit.MILLISECONDS)).thenThrow(new TimeoutException());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> timeoutAspect.handleTimeout(joinPoint));
        assertEquals("操作超时，请稍后重试", ex.getMessage());
        verify(mockFuture, times(1)).cancel(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleTimeout_WrappedRuntimeException_ThrowsOriginal() throws Throwable {
        when(mockExecutor.submit(any(Callable.class))).thenReturn(mockFuture);
        when(mockFuture.get(5000, TimeUnit.MILLISECONDS))
                .thenThrow(new ExecutionException(new IllegalArgumentException("原始异常")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> timeoutAspect.handleTimeout(joinPoint));
        assertEquals("原始异常", ex.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleTimeout_WrappedCheckedException_ThrowsRuntimeException() throws Throwable {
        when(mockExecutor.submit(any(Callable.class))).thenReturn(mockFuture);
        when(mockFuture.get(5000, TimeUnit.MILLISECONDS))
                .thenThrow(new ExecutionException(new Exception("checked")));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> timeoutAspect.handleTimeout(joinPoint));
        assertEquals("checked", ex.getCause().getMessage());
    }
}
