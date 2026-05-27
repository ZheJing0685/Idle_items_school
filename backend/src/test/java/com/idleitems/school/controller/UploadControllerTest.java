package com.idleitems.school.controller;

import com.idleitems.school.service.FileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UploadController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UploadController 接口测试")
class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @Test
    @DisplayName("测试上传图片成功")
    void testUploadSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("url", "http://localhost:7000/uploads/2026/05/07/test.jpg");
        expectedResult.put("path", "2026/05/07/test.jpg");
        when(fileService.uploadImage(any())).thenReturn(expectedResult);

        mockMvc.perform(multipart("/api/upload").file(file)
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.url").exists())
                .andExpect(jsonPath("$.data.path").exists());
    }

    @Test
    @DisplayName("测试上传空文件")
    void testUploadEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/upload").file(file)
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("文件不能为空"));
    }

    @Test
    @DisplayName("测试上传不支持的文件类型")
    void testUploadInvalidFileType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.exe",
                "application/octet-stream",
                "executable content".getBytes()
        );

        when(fileService.uploadImage(any()))
                .thenThrow(new IllegalArgumentException("文件类型不支持，仅支持JPG、PNG格式"));

        mockMvc.perform(multipart("/api/upload").file(file)
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("文件验证失败：文件类型不支持，仅支持JPG、PNG格式"));
    }

    @Test
    @DisplayName("测试上传服务异常")
    void testUploadServiceException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(fileService.uploadImage(any()))
                .thenThrow(new RuntimeException("磁盘空间不足"));

        mockMvc.perform(multipart("/api/upload").file(file)
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("文件上传失败：磁盘空间不足"));
    }
}
