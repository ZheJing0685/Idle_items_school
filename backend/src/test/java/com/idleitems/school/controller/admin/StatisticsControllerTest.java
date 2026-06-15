package com.idleitems.school.controller.admin;

import com.idleitems.school.module.admin.controller.StatisticsController;
import com.idleitems.school.module.admin.dto.DashboardResponse;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.category.repository.CategoryRepository;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.order.repository.OrderRepository;
import com.idleitems.school.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatisticsController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("StatisticsController 统计分析接口测试")
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        User adminUser = new User();
        adminUser.setId(99L);
        adminUser.setRole(User.Role.ADMIN);
        when(userRepository.findById(99L)).thenReturn(Optional.of(adminUser));
    }

    @Test
    @DisplayName("测试获取仪表盘数据")
    void testGetDashboard() throws Exception {
        when(orderRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(orderRepository.count()).thenReturn(100L);
        when(orderRepository.countByOrderStatusGrouped()).thenReturn(List.of());
        when(orderRepository.sumCompletedOrderAmount()).thenReturn(BigDecimal.valueOf(10000));
        when(orderRepository.countOrdersAndAmountGroupedByDate(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(userRepository.findAllById(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/statistics/dashboard")
                        .param("timeRange", "today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取概览数据")
    void testGetOverview() throws Exception {
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.findByStatus(any(User.UserStatus.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 1), 80));
        when(itemRepository.count()).thenReturn(200L);
        when(itemRepository.findByStatus(any(Item.ItemStatus.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 1), 150));
        when(orderRepository.count()).thenReturn(50L);
        when(orderRepository.countByOrderStatus(Order.OrderStatus.COMPLETED)).thenReturn(40L);
        when(orderRepository.sumCompletedOrderAmount()).thenReturn(BigDecimal.valueOf(5000));

        mockMvc.perform(get("/api/admin/statistics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalUsers").value(100))
                .andExpect(jsonPath("$.data.totalItems").value(200));
    }

    @Test
    @DisplayName("测试获取月度统计")
    void testGetMonthlyStatistics() throws Exception {
        when(orderRepository.countCompletedOrdersByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(10L);
        when(orderRepository.sumCompletedOrderAmountByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.valueOf(1000));

        mockMvc.perform(get("/api/admin/statistics/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("testGetCategoryStatistics")
    void testGetCategoryStatistics() throws Exception {
        when(categoryRepository.findAll()).thenReturn(List.of());
        when(itemRepository.countByCategoryIdsGrouped(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/statistics/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("testGetHotItems")
    void testGetHotItems() throws Exception {
        when(itemRepository.findTop10ByStatusOrderByViewCountDesc(Item.ItemStatus.ON_SALE))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/admin/statistics/hot-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
