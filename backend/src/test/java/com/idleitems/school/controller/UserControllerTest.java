package com.idleitems.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.module.user.dto.UpdateProfileRequest;
import com.idleitems.school.module.user.dto.UserDTO;
import com.idleitems.school.module.user.dto.UserStatsDTO;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.item.service.ItemQueryService;
import com.idleitems.school.module.order.service.ReviewService;
import com.idleitems.school.module.user.service.UserService;
import com.idleitems.school.module.user.controller.UserController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController 接口测试")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ItemQueryService itemQueryService;

    @MockitoBean
    private ReviewService reviewService;

    private User testUser;
    private UserDTO testUserDTO;
    private UserStatsDTO testUserStats;
    private UpdateProfileRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setNickname("测试用户");
        testUser.setRole(User.Role.STUDENT);
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser.setVerified(true);

        testUserDTO = UserDTO.fromEntity(testUser);

        testUserStats = UserStatsDTO.builder()
                .totalItems(10L)
                .soldItems(3L)
                .completedDeals(5L)
                .rating(4.5)
                .build();

        updateRequest = new UpdateProfileRequest();
        updateRequest.setNickname("新昵称");
        updateRequest.setPhone("13900139000");
    }

    @Test
    @DisplayName("获取用户信息 - 成功")
    void testGetProfileSuccess() throws Exception {
        when(userService.getUserById(1L)).thenReturn(testUser);

        mockMvc.perform(get("/api/user/profile")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("获取用户信息 - 用户不存在")
    void testGetProfileNotFound() throws Exception {
        when(userService.getUserById(999L))
                .thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));

        mockMvc.perform(get("/api/user/profile")
                        .requestAttr("userId", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    @DisplayName("更新用户信息 - 成功")
    void testUpdateProfileSuccess() throws Exception {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setNickname("新昵称");
        updatedUser.setPhone("13900139000");
        updatedUser.setRole(User.Role.STUDENT);
        updatedUser.setStatus(User.UserStatus.ACTIVE);

        when(userService.updateUser(eq(1L), any(UpdateProfileRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/user/profile")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("更新成功"));
    }

    @Test
    @DisplayName("更新用户信息 - 用户不存在")
    void testUpdateProfileNotFound() throws Exception {
        when(userService.updateUser(eq(999L), any(UpdateProfileRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));

        mockMvc.perform(put("/api/user/profile")
                        .requestAttr("userId", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    @DisplayName("获取用户统计 - 成功")
    void testGetUserStatsSuccess() throws Exception {
        when(userService.getUserStats(1L)).thenReturn(testUserStats);

        mockMvc.perform(get("/api/user/stats")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalItems").value(10))
                .andExpect(jsonPath("$.data.rating").value(4.5));
    }

    @Test
    @DisplayName("获取用户统计 - 用户不存在")
    void testGetUserStatsNotFound() throws Exception {
        when(userService.getUserStats(999L))
                .thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));

        mockMvc.perform(get("/api/user/stats")
                        .requestAttr("userId", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}
