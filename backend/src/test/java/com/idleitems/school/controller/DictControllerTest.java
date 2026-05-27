package com.idleitems.school.controller;

import com.idleitems.school.service.DictService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DictController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("DictController 接口测试")
class DictControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DictService dictService;

    @Test
    @DisplayName("获取所有字典数据 - 成功")
    void testGetAllDictsSuccess() throws Exception {
        Map<String, List<Map<String, Object>>> allDicts = Map.of(
                "item_condition", List.of(Map.of("value", "NEW", "label", "全新")),
                "delivery_method", List.of(Map.of("value", "PICKUP", "label", "当面交易"))
        );
        when(dictService.getAllDicts()).thenReturn(allDicts);

        mockMvc.perform(get("/api/dicts/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取指定类型的字典数据 - 成功")
    void testGetDictByTypeSuccess() throws Exception {
        List<Map<String, Object>> dictList = List.of(
                Map.of("value", "NEW", "label", "全新")
        );
        when(dictService.getDictByType("item_condition")).thenReturn(dictList);

        mockMvc.perform(get("/api/dicts/item_condition"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取字典项标签 - 成功")
    void testGetDictLabelSuccess() throws Exception {
        when(dictService.getDictLabel("item_condition", "NEW")).thenReturn("全新");

        mockMvc.perform(get("/api/dicts/label")
                        .param("typeCode", "item_condition")
                        .param("itemValue", "NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("全新"));
    }

    @Test
    @DisplayName("获取字典选项列表 - 成功")
    void testGetDictOptionsSuccess() throws Exception {
        List<Map<String, String>> options = List.of(
                Map.of("value", "NEW", "label", "全新")
        );
        when(dictService.getDictOptions("item_condition")).thenReturn(options);

        mockMvc.perform(get("/api/dicts/item_condition/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("清除字典缓存 - 成功")
    void testClearDictCacheSuccess() throws Exception {
        doNothing().when(dictService).clearDictCache();

        mockMvc.perform(post("/api/dicts/cache/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("重新加载指定类型的字典缓存 - 成功")
    void testReloadDictCacheSuccess() throws Exception {
        doNothing().when(dictService).reloadDictCache("item_condition");

        mockMvc.perform(post("/api/dicts/item_condition/cache/reload"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
