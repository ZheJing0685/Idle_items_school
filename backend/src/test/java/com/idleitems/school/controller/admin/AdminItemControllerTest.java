package com.idleitems.school.controller.admin;

import com.idleitems.school.dto.ItemDTO;
import com.idleitems.school.entity.Item;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.ReviewRepository;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.service.AdminLogService;
import com.idleitems.school.service.DictService;
import com.idleitems.school.util.CacheManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminItemController 物品管理接口测试")
@SuppressWarnings("deprecation")
class AdminItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("deprecation")
    @MockBean
    private ItemRepository itemRepository;

    @SuppressWarnings("deprecation")
    @MockBean
    private UserRepository userRepository;

    @SuppressWarnings("deprecation")
    @MockBean
    private OrderRepository orderRepository;

    @SuppressWarnings("deprecation")
    @MockBean
    private ReviewRepository reviewRepository;

    @SuppressWarnings("deprecation")
    @MockBean
    private AdminLogService adminLogService;

    @SuppressWarnings("deprecation")
    @MockBean
    private DictService dictService;

    @SuppressWarnings("deprecation")
    @MockBean
    private CacheManager cacheManager;

    @Test
    @DisplayName("测试获取物品列表")
    void testGetItems() throws Exception {
        Item item = buildItem();
        when(itemRepository.findByStatus(any(Item.ItemStatus.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/admin/items")
                        .param("status", "ON_SALE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取物品统计")
    void testGetItemStats() throws Exception {
        when(itemRepository.count()).thenReturn(100L);
        when(itemRepository.findByStatus(any(Item.ItemStatus.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 1), 50));

        mockMvc.perform(get("/api/admin/items/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(100));
    }

    @Test
    @DisplayName("测试审核通过物品")
    void testApproveItem() throws Exception {
        Item item = buildItem();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(dictService.getDictLabel("ITEM_STATUS", "ON_SALE")).thenReturn("在售");

        mockMvc.perform(put("/api/admin/items/1/approve")
                        .requestAttr("userId", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试驳回物品")
    void testRejectItem() throws Exception {
        Item item = buildItem();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(dictService.getDictLabel("ITEM_STATUS", "REJECTED")).thenReturn("已驳回");

        mockMvc.perform(put("/api/admin/items/1/reject")
                        .requestAttr("userId", 99L)
                        .param("reason", "违规内容"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试强制下架物品")
    void testForceOffShelfItem() throws Exception {
        Item item = buildItem();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(dictService.getDictLabel("ITEM_STATUS", "OFF_SHELF")).thenReturn("已下架");

        mockMvc.perform(put("/api/admin/items/1/off-shelf")
                        .requestAttr("userId", 99L)
                        .param("reason", "违规操作"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试删除物品")
    void testDeleteItem() throws Exception {
        Item item = buildItem();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepository.existsByItemId(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/admin/items/1")
                        .requestAttr("userId", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试删除有关联订单的物品 - 应失败")
    void testDeleteItemWithOrders() throws Exception {
        Item item = buildItem();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepository.existsByItemId(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/admin/items/1")
                        .requestAttr("userId", 99L))
                .andExpect(status().isBadRequest());
    }

    private Item buildItem() {
        Item item = new Item();
        item.setId(1L);
        item.setTitle("测试物品");
        item.setDescription("测试描述");
        item.setPrice(BigDecimal.valueOf(99.99));
        item.setStatus(Item.ItemStatus.ON_SALE);
        item.setUserId(1L);
        return item;
    }
}
