package com.idleitems.school.controller.admin;

import com.idleitems.school.aspect.PermissionAspect;
import com.idleitems.school.dto.UserDTO;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.service.AdminLogService;
import com.idleitems.school.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableAspectJAutoProxy
@Import(PermissionAspect.class)
@DisplayName("AdminUserController 用户管理接口测试")
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AdminLogService adminLogService;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        User adminUser = new User();
        adminUser.setId(99L);
        adminUser.setRole(User.Role.ADMIN);
        when(userRepository.findById(99L)).thenReturn(Optional.of(adminUser));
    }

    @Test
    @DisplayName("测试获取用户列表")
    void testGetUsers() throws Exception {
        User user = buildUser();
        when(userRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/admin/users")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].username").value("testuser"));
    }

    @Test
    @DisplayName("测试获取用户统计")
    void testGetUserStats() throws Exception {
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByStatus(User.UserStatus.ACTIVE)).thenReturn(80L);
        when(userRepository.countByVerified(true)).thenReturn(60L);
        when(userRepository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(10L);

        mockMvc.perform(get("/api/admin/users/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(100))
                .andExpect(jsonPath("$.data.active").value(80));
    }

    @Test
    @DisplayName("测试获取用户详情")
    void testGetUser() throws Exception {
        User user = buildUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("测试更新用户状态")
    void testUpdateUserStatus() throws Exception {
        User user = buildUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        mockMvc.perform(put("/api/admin/users/1/status")
                        .requestAttr("userId", 99L)
                        .param("status", "DISABLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试删除用户")
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/admin/users/1")
                        .requestAttr("userId", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPhone("13800138000");
        user.setRole(User.Role.STUDENT);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setVerified(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
