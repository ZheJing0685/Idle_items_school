package com.idleitems.school.controller;

import com.idleitems.school.module.system.entity.SystemConfig;
import com.idleitems.school.module.system.service.ConfigService;
import com.idleitems.school.module.system.controller.ConfigController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConfigController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ConfigController 接口测试")
class ConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConfigService configService;

    private SystemConfig testConfig;

    @BeforeEach
    void setUp() {
        testConfig = new SystemConfig();
        testConfig.setId(1L);
        testConfig.setConfigKey("site.name");
        testConfig.setConfigValue("闲置物品交易平台");
        testConfig.setDescription("站点名称");
        testConfig.setGroupName("general");
        testConfig.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("获取所有配置 - 成功")
    void testGetAllConfigsSuccess() throws Exception {
        when(configService.getAllConfigs()).thenReturn(Map.of("site.name", "闲置物品交易平台"));

        mockMvc.perform(get("/api/configs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取指定配置值 - 成功")
    void testGetConfigSuccess() throws Exception {
        when(configService.getConfig("site.name")).thenReturn("闲置物品交易平台");

        mockMvc.perform(get("/api/configs/site.name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("闲置物品交易平台"));
    }

    @Test
    @DisplayName("获取指定分组的配置 - 成功")
    void testGetConfigsByGroupSuccess() throws Exception {
        when(configService.getConfigsByGroup("general")).thenReturn(Map.of("site.name", "闲置物品交易平台"));

        mockMvc.perform(get("/api/configs/group/general"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("保存或更新配置 - 成功")
    void testSaveConfigSuccess() throws Exception {
        when(configService.saveConfig(eq("site.name"), eq("新名称"), any())).thenReturn(testConfig);

        mockMvc.perform(post("/api/configs")
                        .param("configKey", "site.name")
                        .param("configValue", "新名称"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("保存或更新配置 - 带描述")
    void testSaveConfigWithDescription() throws Exception {
        when(configService.saveConfig(eq("site.name"), eq("新名称"), eq("站点名称")))
                .thenReturn(testConfig);

        mockMvc.perform(post("/api/configs")
                        .param("configKey", "site.name")
                        .param("configValue", "新名称")
                        .param("description", "站点名称"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除配置 - 成功")
    void testDeleteConfigSuccess() throws Exception {
        when(configService.deleteConfig("site.name")).thenReturn(true);

        mockMvc.perform(delete("/api/configs/site.name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("清除配置缓存 - 成功")
    void testClearConfigCacheSuccess() throws Exception {
        doNothing().when(configService).clearConfigCache();

        mockMvc.perform(post("/api/configs/cache/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("重新加载配置缓存 - 成功")
    void testReloadConfigCacheSuccess() throws Exception {
        doNothing().when(configService).reloadConfigCache();

        mockMvc.perform(post("/api/configs/cache/reload"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
