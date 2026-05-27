package com.idleitems.school.controller.admin;

import com.idleitems.school.aspect.PermissionAspect;
import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.User;
import com.idleitems.school.service.AdminLogService;
import com.idleitems.school.service.DictService;
import com.idleitems.school.service.ItemService;
import com.idleitems.school.cache.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableAspectJAutoProxy
@Import(PermissionAspect.class)
@DisplayName("AdminItemController 物品管理接口测试")
class AdminItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private com.idleitems.school.repository.UserRepository userRepository;

    @MockitoBean
    private AdminLogService adminLogService;

    @MockitoBean
    private DictService dictService;

    @MockitoBean
    private CacheService cacheService;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private com.idleitems.school.service.UserService userService;

    @BeforeEach
    void setUp() {
        User adminUser = new User();
        adminUser.setId(99L);
        adminUser.setRole(User.Role.ADMIN);
        when(userRepository.findById(99L)).thenReturn(java.util.Optional.of(adminUser));
    }

    @Test
    @DisplayName("测试获取物品列表")
    void testGetItems() throws Exception {
        Item item = buildItem();
        when(itemService.getAdminItems(any(Pageable.class), any()))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1));
        when(itemService.getSellerItemCount(anyLong())).thenReturn(0);

        mockMvc.perform(get("/api/admin/items")
                        .param("status", "ON_SALE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取物品统计")
    void testGetItemStats() throws Exception {
        when(itemService.countItems()).thenReturn(100L);
        when(itemService.countItemsByStatus(any())).thenReturn(50L);

        mockMvc.perform(get("/api/admin/items/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(100));
    }

    @Test
    @DisplayName("测试审核通过物品")
    void testApproveItem() throws Exception {
        Item item = buildItem();
        when(itemService.getItemById(1L)).thenReturn(item);
        when(itemService.approveItem(1L)).thenReturn(item);

        mockMvc.perform(post("/api/admin/items/1/approve")
                        .requestAttr("userId", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试驳回物品")
    void testRejectItem() throws Exception {
        Item item = buildItem();
        when(itemService.getItemById(1L)).thenReturn(item);
        when(itemService.rejectItem(1L, "违规内容")).thenReturn(item);

        mockMvc.perform(post("/api/admin/items/1/reject")
                        .requestAttr("userId", 99L)
                        .param("reason", "违规内容"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试强制下架物品")
    void testForceOffShelfItem() throws Exception {
        Item item = buildItem();
        when(itemService.getItemById(1L)).thenReturn(item);
        when(itemService.forceOffShelfItem(1L, "违规操作")).thenReturn(item);

        mockMvc.perform(post("/api/admin/items/1/off-shelf")
                        .requestAttr("userId", 99L)
                        .param("reason", "违规操作"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试删除物品")
    void testDeleteItem() throws Exception {
        Item item = buildItem();
        when(itemService.getItemById(1L)).thenReturn(item);
        when(itemService.existsOrderByItemId(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/admin/items/1")
                        .requestAttr("userId", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试删除有关联订单的物品 - 应失败")
    void testDeleteItemWithOrders() throws Exception {
        Item item = buildItem();
        when(itemService.getItemById(1L)).thenReturn(item);
        when(itemService.existsOrderByItemId(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/admin/items/1")
                        .requestAttr("userId", 99L))
                .andExpect(status().isForbidden());
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
