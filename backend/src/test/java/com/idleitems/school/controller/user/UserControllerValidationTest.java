package com.idleitems.school.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idleitems.school.module.user.controller.UserController;
import com.idleitems.school.module.item.service.ItemQueryService;
import com.idleitems.school.module.order.service.ReviewService;
import com.idleitems.school.module.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController 参数校验测试")
class UserControllerValidationTest {

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

    @Test
    @DisplayName("PUT /api/user/profile - nickname超过50字符应返回400")
    void testUpdateProfile_nicknameTooLong() throws Exception {
        String longNickname = "a".repeat(51);
        String jsonBody = "{\"nickname\":\"" + longNickname + "\"}";

        mockMvc.perform(put("/api/user/profile")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.data.nickname").value("昵称最多50个字符"));
    }

    @Test
    @DisplayName("PUT /api/user/profile - phone超过20字符应返回400")
    void testUpdateProfile_phoneTooLong() throws Exception {
        String longPhone = "1".repeat(21);
        String jsonBody = "{\"phone\":\"" + longPhone + "\"}";

        mockMvc.perform(put("/api/user/profile")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.data.phone").value("手机号最多20个字符"));
    }
}
