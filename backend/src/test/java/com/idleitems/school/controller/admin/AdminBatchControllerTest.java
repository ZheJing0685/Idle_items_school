package com.idleitems.school.controller.admin;

import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.Order;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.service.AdminLogService;
import com.idleitems.school.service.DictService;
import com.idleitems.school.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminBatchController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminBatchController 批量操作接口测试")
@SuppressWarnings("deprecation")
class AdminBatchControllerTest {

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
    private OrderService orderService;

    @SuppressWarnings("deprecation")
    @MockBean
    private AdminLogService adminLogService;

    @SuppressWarnings("deprecation")
    @MockBean
    private DictService dictService;

    @Test
    @DisplayName("测试批量审核通过物品")
    void testBatchApproveItems() throws Exception {
        Item item = buildItem();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(dictService.getDictLabel("ITEM_STATUS", "ON_SALE")).thenReturn("在售");

        mockMvc.perform(put("/api/admin/batch/items/approve")
                        .requestAttr("userId", 99L)
                        .contentType("application/json")
                        .content("[1]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试批量驳回物品")
    void testBatchRejectItems() throws Exception {
        Item item = buildItem();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(dictService.getDictLabel("ITEM_STATUS", "REJECTED")).thenReturn("已驳回");

        mockMvc.perform(put("/api/admin/batch/items/reject")
                        .requestAttr("userId", 99L)
                        .contentType("application/json")
                        .content("{\"itemIds\":[1],\"reason\":\"违规内容\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试批量下架物品")
    void testBatchOffShelfItems() throws Exception {
        Item item = buildItem();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(dictService.getDictLabel("ITEM_STATUS", "OFF_SHELF")).thenReturn("已下架");

        mockMvc.perform(put("/api/admin/batch/items/off-shelf")
                        .requestAttr("userId", 99L)
                        .contentType("application/json")
                        .content("{\"itemIds\":[1],\"reason\":\"违规操作\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试批量更新用户状态")
    void testBatchUpdateUserStatus() throws Exception {
        User user = buildUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(put("/api/admin/batch/users/status")
                        .requestAttr("userId", 99L)
                        .contentType("application/json")
                        .content("{\"userIds\":[1],\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试批量取消订单")
    void testBatchCancelOrders() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORD-1");
        when(orderService.adminCancelOrder(1L, 99L, "管理员取消")).thenReturn(order);
        when(dictService.getDictLabel("ORDER_STATUS", "CANCELLED")).thenReturn("已取消");

        mockMvc.perform(put("/api/admin/batch/orders/cancel")
                        .requestAttr("userId", 99L)
                        .contentType("application/json")
                        .content("{\"orderIds\":[1],\"reason\":\"管理员取消\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private Item buildItem() {
        Item item = new Item();
        item.setId(1L);
        item.setTitle("测试物品");
        item.setDescription("测试描述");
        item.setPrice(BigDecimal.valueOf(99.99));
        item.setStatus(Item.ItemStatus.PENDING);
        item.setUserId(1L);
        return item;
    }

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(User.Role.STUDENT);
        user.setStatus(User.UserStatus.ACTIVE);
        return user;
    }
}
