package com.idleitems.school.aspect;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final UserRepository userRepository;

    @Around("@annotation(com.idleitems.school.annotation.RequireRole)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole requireRole = method.getAnnotation(RequireRole.class);

        // 从请求上下文中获取userId
        Long userId = null;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                userId = (Long) attributes.getRequest().getAttribute("userId");
            }
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
        }

        if (userId == null) {
            throw new SecurityException("无法获取用户信息");
        }

        // 查询用户信息
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SecurityException("用户不存在"));

        // 检查用户角色
        boolean hasPermission = Arrays.asList(requireRole.value()).contains(user.getRole());

        if (!hasPermission) {
            log.warn("用户 {} 尝试访问需要角色 {} 的资源，但用户角色为 {}", 
                    userId, Arrays.toString(requireRole.value()), user.getRole());
            throw new SecurityException(requireRole.message());
        }

        return joinPoint.proceed();
    }
}