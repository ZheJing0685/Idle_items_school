package com.idleitems.school.controller;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.dto.FavoriteDTO;
import com.idleitems.school.entity.Favorite;
import com.idleitems.school.service.FavoriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FavoriteController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("FavoriteController 接口测试")
class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FavoriteService favoriteService;

    private Favorite testFavorite;

    @BeforeEach
    void setUp() {
        testFavorite = new Favorite();
        testFavorite.setId(1L);
        testFavorite.setUserId(1L);
        testFavorite.setItemId(1L);
        testFavorite.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("添加收藏 - 成功")
    void testAddFavoriteSuccess() throws Exception {
        when(favoriteService.addFavorite(1L, 1L)).thenReturn(testFavorite);

        mockMvc.perform(post("/api/favorites/1")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("收藏成功"));
    }

    @Test
    @DisplayName("添加收藏 - 物品不存在")
    void testAddFavoriteItemNotFound() throws Exception {
        when(favoriteService.addFavorite(1L, 999L))
                .thenThrow(new BusinessException(ErrorCode.ITEM_NOT_FOUND, "物品不存在"));

        mockMvc.perform(post("/api/favorites/999")
                        .requestAttr("userId", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("物品不存在"));
    }

    @Test
    @DisplayName("取消收藏 - 成功")
    void testRemoveFavoriteSuccess() throws Exception {
        doNothing().when(favoriteService).removeFavorite(1L, 1L);

        mockMvc.perform(delete("/api/favorites/1")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("已取消收藏"));
    }

    @Test
    @DisplayName("获取收藏列表 - 成功")
    void testGetUserFavoritesSuccess() throws Exception {
        Page<FavoriteDTO> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(favoriteService.getUserFavorites(eq(1L), any())).thenReturn(emptyPage);

        mockMvc.perform(get("/api/favorites")
                        .requestAttr("userId", 1L)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("检查收藏状态 - 已收藏")
    void testCheckFavoriteFavorited() throws Exception {
        when(favoriteService.isFavorited(1L, 1L)).thenReturn(true);

        mockMvc.perform(get("/api/favorites/1/status")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("检查收藏状态 - 未收藏")
    void testCheckFavoriteNotFavorited() throws Exception {
        when(favoriteService.isFavorited(1L, 1L)).thenReturn(false);

        mockMvc.perform(get("/api/favorites/1/status")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));
    }
}
