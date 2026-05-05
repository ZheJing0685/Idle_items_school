package com.idleitems.school.aspect;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.UserRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermissionAspectTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature signature;

    @InjectMocks
    private PermissionAspect permissionAspect;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCheckPermissionWithAdminRole() throws Throwable {
        // 准备测试数据
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(User.Role.ADMIN);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(getTestMethod());
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(joinPoint.proceed()).thenReturn("success");

        // 执行测试
        Object result = permissionAspect.checkPermission(joinPoint);

        // 验证结果
        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void testCheckPermissionWithInsufficientRole() throws Throwable {
        // 准备测试数据
        User normalUser = new User();
        normalUser.setId(1L);
        normalUser.setRole(User.Role.STUDENT);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(getTestMethod());
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});
        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));

        // 执行测试并验证异常
        assertThrows(SecurityException.class, () -> {
            try {
                permissionAspect.checkPermission(joinPoint);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        });
    }

    private Method getTestMethod() throws NoSuchMethodException {
        return this.getClass().getDeclaredMethod("testMethod");
    }

    @RequireRole(value = {User.Role.ADMIN})
    public void testMethod() {
        // 测试方法
    }
}