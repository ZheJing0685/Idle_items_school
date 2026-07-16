package com.idleitems.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.module.item.dto.CreateItemRequest;
import com.idleitems.school.module.item.dto.ItemDTO;
import com.idleitems.school.module.item.dto.ItemSummaryDTO;
import com.idleitems.school.module.item.dto.UpdateItemRequest;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.file.service.FileService;
import com.idleitems.school.module.item.service.ItemCommandService;
import com.idleitems.school.module.item.service.ItemQueryService;
import com.idleitems.school.module.item.service.RecommendationService;
import com.idleitems.school.module.order.service.OrderQueryService;
import com.idleitems.school.module.user.service.UserService;
import com.idleitems.school.module.item.controller.ItemController;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ItemController 接口测试")
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemQueryService itemQueryService;

    @MockitoBean
    private ItemCommandService itemCommandService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private FileService fileService;

    @MockitoBean
    private OrderQueryService orderQueryService;

    @MockitoBean
    private RecommendationService recommendationService;

    private Item testItem;
    private CreateItemRequest createRequest;
    private UpdateItemRequest updateRequest;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setUserId(1L);
        testItem.setTitle("测试物品");
        testItem.setDescription("这是一个测试物品的详细描述");
        testItem.setPrice(new BigDecimal("99.99"));
        testItem.setStatus(Item.ItemStatus.ON_SALE);
        testItem.setCategoryId(1L);
        testItem.setCondition(Item.ItemCondition.GOOD);

        createRequest = new CreateItemRequest();
        createRequest.setTitle("测试物品");
        createRequest.setDescription("这是一个测试物品的详细描述，至少十个字符");
        createRequest.setPrice(new BigDecimal("99.99"));
        createRequest.setCategoryId(1L);

        updateRequest = new UpdateItemRequest();
        updateRequest.setTitle("更新后的物品");
        updateRequest.setPrice(new BigDecimal("199.99"));
    }

    @Test
    @DisplayName("获取物品列表 - 成功")
    void testGetItemsSuccess() throws Exception {
        Page<ItemSummaryDTO> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
        when(itemQueryService.getItems(eq(1), eq(20), eq(null), eq("createdAt"), eq(null), eq(null), eq(null)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/items")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("搜索物品 - 成功")
    void testSearchItemsSuccess() throws Exception {
        Page<ItemSummaryDTO> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
        when(itemQueryService.searchItems(eq("手机"), eq(1), eq(20), eq("createdAt")))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/items/search")
                        .param("keyword", "手机")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("搜索物品 - 无结果")
    void testSearchItemsNoResults() throws Exception {
        Page<ItemSummaryDTO> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
        when(itemQueryService.searchItems(eq("不存在的东西"), eq(1), eq(20), eq("createdAt")))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/items/search")
                        .param("keyword", "不存在的东西")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取热门物品 - 成功")
    void testGetHotItemsSuccess() throws Exception {
        when(itemQueryService.getHotItems()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/items/hot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取物品详情 - 成功")
    void testGetItemSuccess() throws Exception {
        when(itemQueryService.getItemById(1L)).thenReturn(testItem);

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("测试物品"));
    }

    @Test
    @DisplayName("获取物品详情 - 物品不存在")
    void testGetItemNotFound() throws Exception {
        when(itemQueryService.getItemById(999L))
                .thenThrow(new BusinessException(ErrorCode.ITEM_NOT_FOUND, "物品不存在"));

        mockMvc.perform(get("/api/items/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("发布物品 - 成功")
    void testCreateItemSuccess() throws Exception {
        when(itemCommandService.createItem(eq(1L), any(CreateItemRequest.class))).thenReturn(testItem);
        when(itemQueryService.getSellerItemCount(1L)).thenReturn(5);

        mockMvc.perform(post("/api/items")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("发布成功，等待审核"));
    }

    @Test
    @DisplayName("发布物品 - 参数校验失败（标题为空）")
    void testCreateItemValidationTitleBlank() throws Exception {
        CreateItemRequest invalidRequest = new CreateItemRequest();
        invalidRequest.setTitle("");
        invalidRequest.setDescription("这是一个测试物品的详细描述，至少十个字符");
        invalidRequest.setPrice(new BigDecimal("99.99"));
        invalidRequest.setCategoryId(1L);

        mockMvc.perform(post("/api/items")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("发布物品 - 参数校验失败（价格为空）")
    void testCreateItemValidationPriceNull() throws Exception {
        CreateItemRequest invalidRequest = new CreateItemRequest();
        invalidRequest.setTitle("测试物品");
        invalidRequest.setDescription("这是一个测试物品的详细描述，至少十个字符");
        invalidRequest.setPrice(null);
        invalidRequest.setCategoryId(1L);

        mockMvc.perform(post("/api/items")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("更新物品 - 成功")
    void testUpdateItemSuccess() throws Exception {
        when(itemCommandService.updateItem(eq(1L), eq(1L), any(UpdateItemRequest.class))).thenReturn(testItem);
        when(itemQueryService.getSellerItemCount(1L)).thenReturn(5);

        mockMvc.perform(put("/api/items/1")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("更新成功，等待审核"));
    }

    @Test
    @DisplayName("更新物品 - 物品不存在")
    void testUpdateItemNotFound() throws Exception {
        when(itemCommandService.updateItem(eq(1L), eq(999L), any(UpdateItemRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.ITEM_NOT_FOUND, "物品不存在"));

        mockMvc.perform(put("/api/items/999")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("下架物品 - 成功")
    void testOffShelfItemSuccess() throws Exception {
        when(itemCommandService.offShelfItem(1L, 1L)).thenReturn(testItem);

        mockMvc.perform(post("/api/items/1/off-shelf")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("下架成功"));
    }

    @Test
    @DisplayName("删除物品 - 成功")
    void testDeleteItemSuccess() throws Exception {
        doNothing().when(itemCommandService).deleteItemByUser(1L, 1L);

        mockMvc.perform(delete("/api/items/1")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));
    }

    @Test
    @DisplayName("获取用户物品列表 - 成功")
    void testGetUserItemsSuccess() throws Exception {
        Page<Item> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
        when(itemQueryService.getUserItems(eq(1L), eq(null), eq(1), eq(20))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/items/user")
                        .requestAttr("userId", 1L)
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取物品订单 - 成功")
    void testGetItemOrdersSuccess() throws Exception {
        when(orderQueryService.getOrdersByItemId(1L, 1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/items/1/orders")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
