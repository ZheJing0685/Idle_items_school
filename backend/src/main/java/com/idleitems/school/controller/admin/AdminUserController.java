package com.idleitems.school.controller.admin;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.common.Result;
import com.idleitems.school.dto.UserDTO;
import com.idleitems.school.dto.admin.CreateUserRequest;
import com.idleitems.school.dto.admin.UpdateUserRequest;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.service.AdminLogService;
import com.idleitems.school.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN}, message = "需要管理员权限")
public class AdminUserController {

    private final UserRepository userRepository;
    private final AdminLogService adminLogService;
    private final UserService userService;

    @GetMapping
    public Result<Page<User>> getUsers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "role", required = false) User.Role role,
            @RequestParam(value = "status", required = false) User.UserStatus userStatus,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder) {
        try {
            log.info("getUsers called - page: {}, size: {}, sortBy: {}, sortOrder: {}", page, size, sortBy, sortOrder);
            
            Sort.Direction direction = Sort.Direction.DESC;
            if ("asc".equalsIgnoreCase(sortOrder)) {
                direction = Sort.Direction.ASC;
            }
            
            String sortField;
            switch (sortBy) {
                case "id":
                    sortField = "id";
                    break;
                case "username":
                    sortField = "username";
                    break;
                case "createdAt":
                default:
                    sortField = "createdAt";
            }
            
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortField));
            log.info("Pageable created: {}", pageable);
            
            Page<User> users;
            if (role != null && userStatus != null) {
                users = userRepository.findByRoleAndStatus(role, userStatus, pageable);
            } else if (role != null) {
                users = userRepository.findByRole(role, pageable);
            } else if (userStatus != null) {
                users = userRepository.findByStatus(userStatus, pageable);
            } else {
                users = userRepository.findAll(pageable);
            }
            
            log.info("Users found: {}", users.getTotalElements());
            return Result.success(users);
        } catch (Exception e) {
            log.error("Error in getUsers", e);
            throw e;
        }
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getUserStats() {
        try {
            long total = userRepository.count();
            long active = userRepository.countByStatus(User.UserStatus.ACTIVE);
            long verified = userRepository.countByVerified(true);
            
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
            long newThisWeek = userRepository.countByCreatedAtAfter(oneWeekAgo);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", total);
            stats.put("active", active);
            stats.put("verified", verified);
            stats.put("newThisWeek", newThisWeek);
            
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<UserDTO> getUser(@PathVariable Long id) {
        User user = userRepository.findById(id.longValue())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return Result.success(UserDTO.fromEntity(user));
    }

    @PutMapping("/{id}/status")
    public Result<UserDTO> updateUserStatus(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestParam User.UserStatus status,
            HttpServletRequest request) {
        User user = userRepository.findById(id.longValue())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        User.UserStatus oldStatus = user.getStatus();
        user.setStatus(status);
        User savedUser = userRepository.save(user);
        
        Map<String, Object> details = new HashMap<>();
        details.put("userId", id);
        details.put("oldStatus", oldStatus);
        details.put("newStatus", status);
        adminLogService.logOperation(adminId, "更新用户状态", "USER", id, details, request);
        
        return Result.success("用户状态已更新", UserDTO.fromEntity(savedUser));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        userRepository.deleteById(id.longValue());

        Map<String, Object> details = new HashMap<>();
        details.put("userId", id);
        adminLogService.logOperation(adminId, "删除用户", "USER", id, details, request);

        return Result.success("用户已删除", null);
    }

    @PostMapping
    public Result<UserDTO> createUser(
            @RequestAttribute("userId") Long adminId,
            @RequestBody @Valid CreateUserRequest request,
            HttpServletRequest httpRequest) {
        try {
            User.Role role = User.Role.valueOf(request.getRole());
            User.UserStatus status = User.UserStatus.valueOf(request.getStatus());
            
            User user = userService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getPhone(),
                role,
                status,
                request.getNickname(),
                request.getStudentId()
            );

            Map<String, Object> details = new HashMap<>();
            details.put("userId", user.getId());
            details.put("username", user.getUsername());
            details.put("email", user.getEmail());
            details.put("role", user.getRole());
            adminLogService.logOperation(adminId, "创建用户", "USER", user.getId(), details, httpRequest);

            return Result.success("用户创建成功", UserDTO.fromEntity(user));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<UserDTO> updateUser(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRequest request,
            HttpServletRequest httpRequest) {
        try {
            User.Role role = request.getRole() != null ? User.Role.valueOf(request.getRole()) : null;
            User.UserStatus status = request.getStatus() != null ? User.UserStatus.valueOf(request.getStatus()) : null;
            
            User user = userService.updateUserAdmin(
                id,
                request.getEmail(),
                request.getPhone(),
                role,
                status,
                request.getNickname(),
                request.getStudentId(),
                request.getGender(),
                request.getBio(),
                request.getSchoolName()
            );

            Map<String, Object> details = new HashMap<>();
            details.put("userId", user.getId());
            details.put("username", user.getUsername());
            details.put("email", user.getEmail());
            details.put("role", user.getRole());
            details.put("status", user.getStatus());
            adminLogService.logOperation(adminId, "更新用户", "USER", user.getId(), details, httpRequest);

            return Result.success("用户更新成功", UserDTO.fromEntity(user));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/export")
    public void exportUsers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) String status,
            HttpServletResponse response) throws IOException {
        User.Role userRole = role != null ? User.Role.valueOf(role) : null;
        User.UserStatus userStatus = status != null ? User.UserStatus.valueOf(status) : null;
        
        List<User> users = userService.getUsersForExport(keyword, userRole, userStatus);
        
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", 
            "attachment;filename=users_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,用户名,邮箱,手机,昵称,角色,状态,学号,创建时间\n");
        
        for (User user : users) {
            csv.append(user.getId()).append(",")
               .append(user.getUsername()).append(",")
               .append(user.getEmail() != null ? user.getEmail() : "").append(",")
               .append(user.getPhone() != null ? user.getPhone() : "").append(",")
               .append(user.getNickname() != null ? user.getNickname() : "").append(",")
               .append(user.getRole()).append(",")
               .append(user.getStatus()).append(",")
               .append(user.getStudentId() != null ? user.getStudentId() : "").append(",")
               .append(user.getCreatedAt() != null ? user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "").append("\n");
        }
        
        response.getOutputStream().write(csv.toString().getBytes("UTF-8"));
        response.getOutputStream().flush();
    }

    @PostMapping("/batch/delete")
    public Result<Void> batchDeleteUsers(
            @RequestAttribute("userId") Long adminId,
            @RequestBody List<Long> userIds,
            HttpServletRequest request) {
        try {
            userService.deleteUsers(userIds);
            
            Map<String, Object> details = new HashMap<>();
            details.put("userIds", userIds);
            details.put("count", userIds.size());
            adminLogService.logOperation(adminId, "批量删除用户", "USER", null, details, request);
            
            return Result.success("批量删除成功", null);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
