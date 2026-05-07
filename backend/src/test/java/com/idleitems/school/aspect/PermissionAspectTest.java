package com.idleitems.school.aspect;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private PermissionAspect permissionAspect;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(getTestMethod());
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void testCheckPermissionWithAdminRole() throws Throwable {
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(User.Role.ADMIN);

        when(request.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(joinPoint.proceed()).thenReturn("success");

        Object result = permissionAspect.checkPermission(joinPoint);

        assertEquals("success", result);
        verify(joinPoint).proceed();
    }

    @Test
    void testCheckPermissionWithInsufficientRole() throws Throwable {
        User normalUser = new User();
        normalUser.setId(1L);
        normalUser.setRole(User.Role.STUDENT);

        when(request.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));

        assertThrows(SecurityException.class, () -> permissionAspect.checkPermission(joinPoint));
    }

    private Method getTestMethod() {
        try {
            return this.getClass().getDeclaredMethod("testMethod");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @RequireRole(value = {User.Role.ADMIN})
    public void testMethod() {
    }
}
