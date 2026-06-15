package com.idleitems.school.controller.admin;

import com.idleitems.school.module.admin.controller.MonitorController;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MonitorController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("MonitorController 系统监控接口测试")
class MonitorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeterRegistry meterRegistry;

    @MockitoBean
    private HikariDataSource dataSource;

    @Test
    @DisplayName("获取系统指标成功")
    void getSystemMetrics_returnsMetrics() throws Exception {
        HikariPoolMXBean poolMXBean = org.mockito.Mockito.mock(HikariPoolMXBean.class);
        when(poolMXBean.getActiveConnections()).thenReturn(5);
        when(poolMXBean.getIdleConnections()).thenReturn(10);
        when(poolMXBean.getTotalConnections()).thenReturn(15);
        when(dataSource.getHikariPoolMXBean()).thenReturn(poolMXBean);
        when(dataSource.getMaximumPoolSize()).thenReturn(20);

        mockMvc.perform(get("/api/admin/monitor/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.heapMemory").exists())
                .andExpect(jsonPath("$.data.heapMemory.used").isNumber())
                .andExpect(jsonPath("$.data.heapMemory.committed").isNumber())
                .andExpect(jsonPath("$.data.heapMemory.max").isNumber())
                .andExpect(jsonPath("$.data.heapMemory.usagePercent").isNumber())
                .andExpect(jsonPath("$.data.nonHeapMemory").exists())
                .andExpect(jsonPath("$.data.nonHeapMemory.used").isNumber())
                .andExpect(jsonPath("$.data.threads").exists())
                .andExpect(jsonPath("$.data.threads.count").isNumber())
                .andExpect(jsonPath("$.data.threads.daemonCount").isNumber())
                .andExpect(jsonPath("$.data.threads.peakCount").isNumber())
                .andExpect(jsonPath("$.data.classLoading").exists())
                .andExpect(jsonPath("$.data.classLoading.loadedClassCount").isNumber())
                .andExpect(jsonPath("$.data.runtime").exists())
                .andExpect(jsonPath("$.data.runtime.uptime").isNumber())
                .andExpect(jsonPath("$.data.runtime.javaVersion").isString())
                .andExpect(jsonPath("$.data.database").exists())
                .andExpect(jsonPath("$.data.database.activeConnections").value(5))
                .andExpect(jsonPath("$.data.database.idleConnections").value(10))
                .andExpect(jsonPath("$.data.database.totalConnections").value(15))
                .andExpect(jsonPath("$.data.database.maxConnections").value(20));
    }
}
