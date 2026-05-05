# 后台管理系统功能完善实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完善后台管理系统的功能，包括API文档生成、权限控制优化、性能优化、日志分析和系统监控

**Architecture:** 基于现有的Spring Boot架构，添加Swagger API文档、实现基于角色的权限控制、优化数据库查询、增加日志分析功能和系统监控功能

**Tech Stack:** Spring Boot 3.2+, Spring Security, Spring Data JPA, Swagger/OpenAPI 3.0, MySQL 8.0+, Redis, Micrometer

---

## 文件结构

### 新增文件
- `backend/src/main/java/com/idleitems/school/config/SwaggerConfig.java` - Swagger配置
- `backend/src/main/java/com/idleitems/school/config/SecurityConfig.java` - 安全配置（权限控制）
- `backend/src/main/java/com/idleitems/school/annotation/RequireRole.java` - 权限注解
- `backend/src/main/java/com/idleitems/school/aspect/PermissionAspect.java` - 权限切面
- `backend/src/main/java/com/idleitems/school/service/LogAnalysisService.java` - 日志分析服务
- `backend/src/main/java/com/idleitems/school/controller/admin/LogAnalysisController.java` - 日志分析控制器
- `backend/src/main/java/com/idleitems/school/controller/admin/MonitorController.java` - 系统监控控制器
- `backend/src/main/java/com/idleitems/school/dto/analytics/LogAnalysisResponse.java` - 日志分析响应DTO
- `backend/src/main/java/com/idleitems/school/dto/analytics/SystemMetricsResponse.java` - 系统指标响应DTO

### 修改文件
- `backend/pom.xml` - 添加Swagger、Micrometer依赖
- `backend/src/main/java/com/idleitems/school/controller/admin/AdminController.java` - 添加权限注解
- `backend/src/main/java/com/idleitems/school/repository/AdminLogRepository.java` - 添加统计查询方法
- `backend/src/main/resources/application.yml` - 添加Swagger和监控配置

---

## Task 1: 添加Swagger API文档支持

**Files:**
- Modify: `backend/pom.xml`
- Create: `backend/src/main/java/com/idleitems/school/config/SwaggerConfig.java`
- Modify: `backend/src/main/resources/application.yml`

- [ ] **Step 1: 在pom.xml中添加Swagger依赖**

```xml
<!-- 在 <dependencies> 标签内添加 -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

- [ ] **Step 2: 创建SwaggerConfig.java配置类**

```java
package com.idleitems.school.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:7000}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("闲置物品校园交易平台 API")
                        .version("1.0.0")
                        .description("闲置物品校园交易平台的后台管理API文档")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("dev@idleitems.school"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("开发服务器")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
```

- [ ] **Step 3: 在application.yml中添加Swagger配置**

```yaml
# 在文件末尾添加
springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    tags-sorter: alpha
    operations-sorter: alpha
  packages-to-scan: com.idleitems.school.controller
  paths-to-match: /api/**
```

- [ ] **Step 4: 重启后端服务器并验证Swagger UI**

运行命令: `mvn spring-boot:run`
访问: `http://localhost:7000/swagger-ui.html`
预期结果: 能够看到API文档界面

- [ ] **Step 5: 提交代码**

```bash
git add backend/pom.xml backend/src/main/java/com/idleitems/school/config/SwaggerConfig.java backend/src/main/resources/application.yml
git commit -m "feat: 添加Swagger API文档支持"
```

---

## Task 2: 实现基于角色的权限控制

**Files:**
- Create: `backend/src/main/java/com/idleitems/school/annotation/RequireRole.java`
- Create: `backend/src/main/java/com/idleitems/school/aspect/PermissionAspect.java`
- Modify: `backend/src/main/java/com/idleitems/school/controller/admin/AdminController.java`

- [ ] **Step 1: 创建RequireRole注解**

```java
package com.idleitems.school.annotation;

import com.idleitems.school.entity.User;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    User.Role[] value() default {};
    String message() default "权限不足";
}
```

- [ ] **Step 2: 创建PermissionAspect切面**

```java
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

        // 获取当前用户ID
        Long userId = null;
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Long) {
                userId = (Long) arg;
                break;
            }
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
```

- [ ] **Step 3: 在AdminController中添加权限注解**

```java
// 在类上添加注解
@RequireRole(value = {User.Role.ADMIN, User.Role.SUPER_ADMIN}, message = "需要管理员权限")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    // ... 现有代码

    // 在特定方法上添加更严格的权限控制
    @RequireRole(value = {User.Role.SUPER_ADMIN}, message = "需要超级管理员权限")
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        // 删除用户逻辑
        return Result.success("用户已删除");
    }
}
```

- [ ] **Step 4: 编写权限控制测试**

创建文件: `backend/src/test/java/com/idleitems/school/aspect/PermissionAspectTest.java`

```java
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
    void testCheckPermissionWithInsufficientRole() {
        // 准备测试数据
        User normalUser = new User();
        normalUser.setId(1L);
        normalUser.setRole(User.Role.USER);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(getTestMethod());
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});
        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));

        // 执行测试并验证异常
        assertThrows(SecurityException.class, () -> {
            permissionAspect.checkPermission(joinPoint);
        });
    }

    private Method getTestMethod() throws NoSuchMethodException {
        return this.getClass().getDeclaredMethod("testMethod");
    }

    @RequireRole(value = {User.Role.ADMIN, User.Role.SUPER_ADMIN})
    public void testMethod() {
        // 测试方法
    }
}
```

- [ ] **Step 5: 运行测试**

运行命令: `mvn test -Dtest=PermissionAspectTest`
预期结果: 所有测试通过

- [ ] **Step 6: 提交代码**

```bash
git add backend/src/main/java/com/idleitems/school/annotation/RequireRole.java backend/src/main/java/com/idleitems/school/aspect/PermissionAspect.java backend/src/main/java/com/idleitems/school/controller/admin/AdminController.java backend/src/test/java/com/idleitems/school/aspect/PermissionAspectTest.java
git commit -m "feat: 实现基于角色的权限控制"
```

---

## Task 3: 优化大数据量查询性能

**Files:**
- Modify: `backend/src/main/java/com/idleitems/school/repository/AdminLogRepository.java`
- Modify: `backend/src/main/java/com/idleitems/school/repository/OrderRepository.java`
- Modify: `backend/src/main/java/com/idleitems/school/controller/admin/AdminController.java`

- [ ] **Step 1: 在AdminLogRepository中添加索引优化查询**

```java
// 在 AdminLogRepository.java 中添加以下方法
@Query("SELECT al FROM AdminLog al WHERE al.adminId = :adminId ORDER BY al.createdAt DESC")
Page<AdminLog> findByAdminIdOrderByCreatedAtDesc(@Param("adminId") Long adminId, Pageable pageable);

@Query("SELECT COUNT(al) FROM AdminLog al WHERE al.createdAt >= :startDate")
Long countByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);

@Query("SELECT al.targetType, COUNT(al) as count FROM AdminLog al GROUP BY al.targetType")
List<Object[]> countGroupByTargetType();
```

- [ ] **Step 2: 在OrderRepository中添加索引优化查询**

```java
// 在 OrderRepository.java 中添加以下方法
@Query("SELECT o FROM Order o WHERE o.orderStatus = :status ORDER BY o.createdAt DESC")
Page<Order> findByOrderStatusOrderByCreatedAtDesc(@Param("status") OrderStatus status, Pageable pageable);

@Query("SELECT o FROM Order o WHERE o.buyerId = :buyerId OR o.sellerId = :sellerId ORDER BY o.createdAt DESC")
Page<Order> findByBuyerIdOrSellerIdOrderByCreatedAtDesc(@Param("buyerId") Long buyerId, @Param("sellerId") Long sellerId, Pageable pageable);

@Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate AND o.orderStatus = :status")
Long countByCreatedAtAfterAndOrderStatus(@Param("startDate") LocalDateTime startDate, @Param("status") OrderStatus status);
```

- [ ] **Step 3: 在AdminController中优化查询逻辑**

```java
// 在 AdminController.java 中修改 getAdminLogs 方法
@GetMapping("/logs")
public Result<Page<AdminLog>> getAdminLogs(
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "adminId", required = false) Long adminId) {
    
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<AdminLog> logs;
    
    if (adminId != null) {
        logs = adminLogService.getAdminLogsByAdminId(adminId, pageable);
    } else if (keyword != null && !keyword.isEmpty()) {
        logs = adminLogService.searchAdminLogs(keyword, pageable);
    } else {
        logs = adminLogService.getAdminLogs(pageable);
    }
    
    return Result.success(logs);
}
```

- [ ] **Step 4: 添加数据库索引**

创建文件: `backend/src/main/resources/db/migration/V9__add_performance_indexes.sql`

```sql
-- 为 admin_logs 表添加索引
CREATE INDEX idx_admin_logs_admin_id ON admin_logs(admin_id);
CREATE INDEX idx_admin_logs_created_at ON admin_logs(created_at);
CREATE INDEX idx_admin_logs_target_type ON admin_logs(target_type);

-- 为 orders 表添加索引
CREATE INDEX idx_orders_order_status ON orders(order_status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_buyer_id ON orders(buyer_id);
CREATE INDEX idx_orders_seller_id ON orders(seller_id);

-- 为 items 表添加索引
CREATE INDEX idx_items_status ON items(status);
CREATE INDEX idx_items_user_id ON items(user_id);
CREATE INDEX idx_items_created_at ON items(created_at);
CREATE INDEX idx_items_category_id ON items(category_id);

-- 为 users 表添加索引
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_created_at ON users(created_at);
```

- [ ] **Step 5: 运行数据库迁移**

运行命令: `mvn flyway:migrate`
预期结果: 数据库迁移成功

- [ ] **Step 6: 提交代码**

```bash
git add backend/src/main/java/com/idleitems/school/repository/AdminLogRepository.java backend/src/main/java/com/idleitems/school/repository/OrderRepository.java backend/src/main/java/com/idleitems/school/controller/admin/AdminController.java backend/src/main/resources/db/migration/V9__add_performance_indexes.sql
git commit -m "perf: 优化大数据量查询性能，添加数据库索引"
```

---

## Task 4: 增加日志分析功能

**Files:**
- Create: `backend/src/main/java/com/idleitems/school/service/LogAnalysisService.java`
- Create: `backend/src/main/java/com/idleitems/school/controller/admin/LogAnalysisController.java`
- Create: `backend/src/main/java/com/idleitems/school/dto/analytics/LogAnalysisResponse.java`

- [ ] **Step 1: 创建LogAnalysisResponse DTO**

```java
package com.idleitems.school.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogAnalysisResponse {
    private Long totalOperations;
    private Long todayOperations;
    private Long weekOperations;
    private Long monthOperations;
    private List<OperationCount> operationCounts;
    private List<DailyCount> dailyCounts;
    private List<TargetTypeCount> targetTypeCounts;
    private List<AdminActivity> topAdmins;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationCount {
        private String operation;
        private Long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyCount {
        private LocalDateTime date;
        private Long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetTypeCount {
        private String targetType;
        private Long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminActivity {
        private Long adminId;
        private String adminName;
        private Long operationCount;
    }
}
```

- [ ] **Step 2: 创建LogAnalysisService服务**

```java
package com.idleitems.school.service;

import com.idleitems.school.dto.analytics.LogAnalysisResponse;
import com.idleitems.school.entity.AdminLog;
import com.idleitems.school.repository.AdminLogRepository;
import com.idleitems.school.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogAnalysisService {

    private final AdminLogRepository adminLogRepository;
    private final UserRepository userRepository;

    public LogAnalysisResponse getLogAnalysis() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime weekStart = now.minusWeeks(1);
        LocalDateTime monthStart = now.minusMonths(1);

        // 统计总数
        Long totalOperations = adminLogRepository.count();
        Long todayOperations = adminLogRepository.countByCreatedAtAfter(todayStart);
        Long weekOperations = adminLogRepository.countByCreatedAtAfter(weekStart);
        Long monthOperations = adminLogRepository.countByCreatedAtAfter(monthStart);

        // 统计操作类型
        List<Object[]> operationCounts = adminLogRepository.countGroupByOperation();
        List<LogAnalysisResponse.OperationCount> operationCountList = operationCounts.stream()
                .map(obj -> LogAnalysisResponse.OperationCount.builder()
                        .operation((String) obj[0])
                        .count((Long) obj[1])
                        .build())
                .collect(Collectors.toList());

        // 统计目标类型
        List<Object[]> targetTypeCounts = adminLogRepository.countGroupByTargetType();
        List<LogAnalysisResponse.TargetTypeCount> targetTypeCountList = targetTypeCounts.stream()
                .map(obj -> LogAnalysisResponse.TargetTypeCount.builder()
                        .targetType((String) obj[0])
                        .count((Long) obj[1])
                        .build())
                .collect(Collectors.toList());

        // 统计每日操作数（最近7天）
        List<LogAnalysisResponse.DailyCount> dailyCounts = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.plusDays(1);
            Long count = adminLogRepository.countByCreatedAtBetween(dayStart, dayEnd);
            dailyCounts.add(LogAnalysisResponse.DailyCount.builder()
                    .date(dayStart)
                    .count(count)
                    .build());
        }

        // 统计活跃管理员（操作次数前5）
        List<Object[]> topAdminOperations = adminLogRepository.findTop5AdminsByOperationCount();
        List<LogAnalysisResponse.AdminActivity> topAdmins = topAdminOperations.stream()
                .map(obj -> {
                    Long adminId = (Long) obj[0];
                    Long count = (Long) obj[1];
                    String adminName = userRepository.findById(adminId)
                            .map(user -> user.getUsername())
                            .orElse("未知用户");
                    return LogAnalysisResponse.AdminActivity.builder()
                            .adminId(adminId)
                            .adminName(adminName)
                            .operationCount(count)
                            .build();
                })
                .collect(Collectors.toList());

        return LogAnalysisResponse.builder()
                .totalOperations(totalOperations)
                .todayOperations(todayOperations)
                .weekOperations(weekOperations)
                .monthOperations(monthOperations)
                .operationCounts(operationCountList)
                .dailyCounts(dailyCounts)
                .targetTypeCounts(targetTypeCountList)
                .topAdmins(topAdmins)
                .build();
    }
}
```

- [ ] **Step 3: 在AdminLogRepository中添加统计方法**

```java
// 在 AdminLogRepository.java 中添加以下方法
@Query("SELECT al.operation, COUNT(al) FROM AdminLog al GROUP BY al.operation ORDER BY COUNT(al) DESC")
List<Object[]> countGroupByOperation();

@Query("SELECT al.adminId, COUNT(al) FROM AdminLog al GROUP BY al.adminId ORDER BY COUNT(al) DESC LIMIT 5")
List<Object[]> findTop5AdminsByOperationCount();

Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
```

- [ ] **Step 4: 创建LogAnalysisController控制器**

```java
package com.idleitems.school.controller.admin;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.common.Result;
import com.idleitems.school.dto.analytics.LogAnalysisResponse;
import com.idleitems.school.entity.User;
import com.idleitems.school.service.LogAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/logs/analysis")
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN, User.Role.SUPER_ADMIN})
public class LogAnalysisController {

    private final LogAnalysisService logAnalysisService;

    @GetMapping
    public Result<LogAnalysisResponse> getLogAnalysis() {
        return Result.success(logAnalysisService.getLogAnalysis());
    }
}
```

- [ ] **Step 5: 编写日志分析测试**

创建文件: `backend/src/test/java/com/idleitems/school/service/LogAnalysisServiceTest.java`

```java
package com.idleitems.school.service;

import com.idleitems.school.dto.analytics.LogAnalysisResponse;
import com.idleitems.school.repository.AdminLogRepository;
import com.idleitems.school.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogAnalysisServiceTest {

    @Mock
    private AdminLogRepository adminLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LogAnalysisService logAnalysisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetLogAnalysis() {
        // 准备测试数据
        when(adminLogRepository.count()).thenReturn(100L);
        when(adminLogRepository.countByCreatedAtAfter(any())).thenReturn(10L);
        when(adminLogRepository.countGroupByOperation()).thenReturn(new ArrayList<>());
        when(adminLogRepository.countGroupByTargetType()).thenReturn(new ArrayList<>());
        when(adminLogRepository.countByCreatedAtBetween(any(), any())).thenReturn(5L);
        when(adminLogRepository.findTop5AdminsByOperationCount()).thenReturn(new ArrayList<>());

        // 执行测试
        LogAnalysisResponse response = logAnalysisService.getLogAnalysis();

        // 验证结果
        assertNotNull(response);
        assertEquals(100L, response.getTotalOperations());
        verify(adminLogRepository, times(1)).count();
    }
}
```

- [ ] **Step 6: 运行测试**

运行命令: `mvn test -Dtest=LogAnalysisServiceTest`
预期结果: 所有测试通过

- [ ] **Step 7: 提交代码**

```bash
git add backend/src/main/java/com/idleitems/school/service/LogAnalysisService.java backend/src/main/java/com/idleitems/school/controller/admin/LogAnalysisController.java backend/src/main/java/com/idleitems/school/dto/analytics/LogAnalysisResponse.java backend/src/main/java/com/idleitems/school/repository/AdminLogRepository.java backend/src/test/java/com/idleitems/school/service/LogAnalysisServiceTest.java
git commit -m "feat: 增加日志分析功能"
```

---

## Task 5: 增加系统监控功能

**Files:**
- Modify: `backend/pom.xml`
- Create: `backend/src/main/java/com/idleitems/school/controller/admin/MonitorController.java`
- Create: `backend/src/main/java/com/idleitems/school/dto/analytics/SystemMetricsResponse.java`
- Modify: `backend/src/main/resources/application.yml`

- [ ] **Step 1: 在pom.xml中添加Micrometer依赖**

```xml
<!-- 在 <dependencies> 标签内添加 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

- [ ] **Step 2: 创建SystemMetricsResponse DTO**

```java
package com.idleitems.school.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.management.MemoryUsage;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMetricsResponse {
    private MemoryInfo heapMemory;
    private MemoryInfo nonHeapMemory;
    private ThreadInfo threads;
    private ClassLoadingInfo classLoading;
    private RuntimeInfo runtime;
    private DatabaseInfo database;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemoryInfo {
        private Long used;
        private Long committed;
        private Long max;
        private Double usagePercent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThreadInfo {
        private Integer count;
        private Integer daemonCount;
        private Integer peakCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassLoadingInfo {
        private Integer loadedClassCount;
        private Long totalLoadedClassCount;
        private Long unloadedClassCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuntimeInfo {
        private Long uptime;
        private Long startTime;
        private String javaVersion;
        private String javaVendor;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseInfo {
        private Integer activeConnections;
        private Integer idleConnections;
        private Integer totalConnections;
        private Long maxConnections;
    }
}
```

- [ ] **Step 3: 创建MonitorController控制器**

```java
package com.idleitems.school.controller.admin;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.common.Result;
import com.idleitems.school.dto.analytics.SystemMetricsResponse;
import com.idleitems.school.entity.User;
import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;

@RestController
@RequestMapping("/api/admin/monitor")
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN, User.Role.SUPER_ADMIN})
public class MonitorController {

    private final MeterRegistry meterRegistry;
    private final HikariDataSource dataSource;

    @GetMapping("/metrics")
    public Result<SystemMetricsResponse> getSystemMetrics() {
        // 获取内存信息
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        SystemMetricsResponse.MemoryInfo heapMemory = getMemoryInfo(memoryMXBean.getHeapMemoryUsage());
        SystemMetricsResponse.MemoryInfo nonHeapMemory = getMemoryInfo(memoryMXBean.getNonHeapMemoryUsage());

        // 获取线程信息
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        SystemMetricsResponse.ThreadInfo threads = SystemMetricsResponse.ThreadInfo.builder()
                .count(threadMXBean.getThreadCount())
                .daemonCount(threadMXBean.getDaemonThreadCount())
                .peakCount(threadMXBean.getPeakThreadCount())
                .build();

        // 获取类加载信息
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        SystemMetricsResponse.ClassLoadingInfo classLoading = SystemMetricsResponse.ClassLoadingInfo.builder()
                .loadedClassCount(classLoadingMXBean.getLoadedClassCount())
                .totalLoadedClassCount(classLoadingMXBean.getTotalLoadedClassCount())
                .unloadedClassCount(classLoadingMXBean.getUnloadedClassCount())
                .build();

        // 获取运行时信息
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        SystemMetricsResponse.RuntimeInfo runtime = SystemMetricsResponse.RuntimeInfo.builder()
                .uptime(runtimeMXBean.getUptime())
                .startTime(runtimeMXBean.getStartTime())
                .javaVersion(System.getProperty("java.version"))
                .javaVendor(System.getProperty("java.vendor"))
                .build();

        // 获取数据库连接池信息
        SystemMetricsResponse.DatabaseInfo database = SystemMetricsResponse.DatabaseInfo.builder()
                .activeConnections(dataSource.getHikariPoolMXBean().getActiveConnections())
                .idleConnections(dataSource.getHikariPoolMXBean().getIdleConnections())
                .totalConnections(dataSource.getHikariPoolMXBean().getTotalConnections())
                .maxConnections((long) dataSource.getMaximumPoolSize())
                .build();

        SystemMetricsResponse response = SystemMetricsResponse.builder()
                .heapMemory(heapMemory)
                .nonHeapMemory(nonHeapMemory)
                .threads(threads)
                .classLoading(classLoading)
                .runtime(runtime)
                .database(database)
                .build();

        return Result.success(response);
    }

    private SystemMetricsResponse.MemoryInfo getMemoryInfo(java.lang.management.MemoryUsage memoryUsage) {
        long used = memoryUsage.getUsed();
        long committed = memoryUsage.getCommitted();
        long max = memoryUsage.getMax();
        double usagePercent = max > 0 ? (double) used / max * 100 : 0;

        return SystemMetricsResponse.MemoryInfo.builder()
                .used(used)
                .committed(committed)
                .max(max)
                .usagePercent(usagePercent)
                .build();
    }
}
```

- [ ] **Step 4: 在application.yml中添加监控配置**

```yaml
# 在文件末尾添加
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
    metrics:
      enabled: true
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
```

- [ ] **Step 5: 重启后端服务器并验证监控端点**

运行命令: `mvn spring-boot:run`
访问: `http://localhost:7000/actuator/health`
访问: `http://localhost:7000/actuator/metrics`
访问: `http://localhost:7000/actuator/prometheus`
预期结果: 能够看到系统监控信息

- [ ] **Step 6: 提交代码**

```bash
git add backend/pom.xml backend/src/main/java/com/idleitems/school/controller/admin/MonitorController.java backend/src/main/java/com/idleitems/school/dto/analytics/SystemMetricsResponse.java backend/src/main/resources/application.yml
git commit -m "feat: 增加系统监控功能"
```

---

## 验证清单

- [ ] Swagger UI 能够正常访问并显示API文档
- [ ] 权限控制能够正确拦截未授权的请求
- [ ] 数据库索引创建成功，查询性能提升
- [ ] 日志分析功能能够正确统计和分析操作日志
- [ ] 系统监控功能能够正确显示系统运行状态
- [ ] 所有单元测试通过
- [ ] 所有集成测试通过

---

## 完成标准

1. 所有功能都已实现并通过测试
2. 代码已提交到版本控制系统
3. API文档已生成并可以访问
4. 系统监控和日志分析功能正常工作
5. 权限控制机制正常工作
6. 性能优化措施已生效
