package com.idleitems.school.controller.admin;

import com.idleitems.school.module.admin.controller.StatisticsController;
import com.idleitems.school.module.admin.service.StatisticsService;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatisticsController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@DisplayName("StatisticsController 统计分析接口测试")
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatisticsService statisticsService;

    @Test
    @DisplayName("测试获取仪表盘数据")
    void testGetDashboard() throws Exception {
        when(statisticsService.getDashboard(any(), any(), any()))
                .thenReturn(null);

        mockMvc.perform(get("/api/admin/statistics/dashboard")
                        .param("timeRange", "today"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试获取概览数据")
    void testGetOverview() throws Exception {
        Map<String, Object> overview = Map.of(
            "totalUsers", 100L,
            "activeUsers", 80L,
            "totalItems", 200L,
            "onSaleItems", 150L,
            "totalOrders", 50L,
            "completedOrders", 40L,
            "totalAmount", BigDecimal.valueOf(5000)
        );
        when(statisticsService.getOverview()).thenReturn(overview);

        mockMvc.perform(get("/api/admin/statistics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalUsers").value(100))
                .andExpect(jsonPath("$.data.totalItems").value(200));
    }

    @Test
    @DisplayName("测试获取月度统计")
    void testGetMonthlyStatistics() throws Exception {
        when(statisticsService.getMonthlyStatistics())
                .thenReturn(Map.of("monthlyData", List.of()));

        mockMvc.perform(get("/api/admin/statistics/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("testGetCategoryStatistics")
    void testGetCategoryStatistics() throws Exception {
        when(statisticsService.getCategoryStatistics())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/admin/statistics/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("testGetHotItems")
    void testGetHotItems() throws Exception {
        when(statisticsService.getHotItems())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/admin/statistics/hot-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
