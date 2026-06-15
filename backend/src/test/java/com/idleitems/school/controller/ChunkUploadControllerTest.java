package com.idleitems.school.controller;

import com.idleitems.school.module.file.service.ChunkUploadService;
import com.idleitems.school.module.file.service.FileService;
import com.idleitems.school.module.file.controller.ChunkUploadController;
import com.idleitems.school.util.FileValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChunkUploadController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ChunkUploadController 接口测试")
class ChunkUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChunkUploadService chunkUploadService;

    @MockitoBean
    private FileService fileService;

    @MockitoBean
    private FileValidationService fileValidationService;

    @Test
    @DisplayName("测试分片上传成功")
    void testUploadChunkSuccess() throws Exception {
        MockMultipartFile chunk = new MockMultipartFile(
                "chunk",
                "chunk0",
                "application/octet-stream",
                "chunk data".getBytes()
        );

        doNothing().when(chunkUploadService).saveChunk(anyString(), anyInt(), any());
        when(chunkUploadService.getUploadedChunks(anyString())).thenReturn(Arrays.asList(0));
        when(chunkUploadService.isUploadComplete(anyString(), anyInt())).thenReturn(false);

        mockMvc.perform(multipart("/api/items/upload/chunk")
                .file(chunk)
                .param("fileHash", "abc123")
                .param("chunkIndex", "0")
                .param("totalChunks", "5")
                .param("originalFileName", "test.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.uploadedChunks").isArray())
                .andExpect(jsonPath("$.data.isComplete").value(false));
    }

    @Test
    @DisplayName("测试分片上传失败")
    void testUploadChunkFailure() throws Exception {
        MockMultipartFile chunk = new MockMultipartFile(
                "chunk",
                "chunk0",
                "application/octet-stream",
                "chunk data".getBytes()
        );

        doThrow(new RuntimeException("磁盘写入失败")).when(chunkUploadService)
                .saveChunk(anyString(), anyInt(), any());

        mockMvc.perform(multipart("/api/items/upload/chunk")
                .file(chunk)
                .param("fileHash", "abc123")
                .param("chunkIndex", "0")
                .param("totalChunks", "5")
                .param("originalFileName", "test.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("分片上传失败")));
    }

    @Test
    @DisplayName("测试检查已上传分片")
    void testCheckUploadedChunks() throws Exception {
        when(chunkUploadService.getUploadedChunks(anyString())).thenReturn(Arrays.asList(0, 1, 2));
        when(chunkUploadService.isUploadComplete(anyString(), anyInt())).thenReturn(false);

        mockMvc.perform(get("/api/items/upload/check")
                .param("fileHash", "abc123")
                .param("totalChunks", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.uploadedChunks").isArray())
                .andExpect(jsonPath("$.data.uploadedChunks.length()").value(3))
                .andExpect(jsonPath("$.data.isComplete").value(false));
    }

    @Test
    @DisplayName("测试检查已上传分片失败")
    void testCheckUploadedChunksFailure() throws Exception {
        when(chunkUploadService.getUploadedChunks(anyString()))
                .thenThrow(new RuntimeException("读取分片失败"));

        mockMvc.perform(get("/api/items/upload/check")
                .param("fileHash", "abc123")
                .param("totalChunks", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("检查失败")));
    }

    @Test
    @DisplayName("测试合并分片成功")
    void testCompleteUploadSuccess() throws Exception {
        File tempFile = File.createTempFile("merged", ".jpg");
        tempFile.deleteOnExit();

        when(chunkUploadService.isUploadComplete(anyString(), anyInt())).thenReturn(true);
        when(chunkUploadService.mergeChunks(anyString(), anyInt(), anyString())).thenReturn(tempFile);
        when(fileValidationService.validateImage(any())).thenReturn(new FileValidationService.ImageValidationResult(100, 100, "jpg"));

        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("url", "http://localhost:7000/uploads/2026/05/07/test.jpg");
        expectedResult.put("path", "2026/05/07/test.jpg");
        when(fileService.uploadImage(any())).thenReturn(expectedResult);

        mockMvc.perform(post("/api/items/upload/complete")
                .param("fileHash", "abc123")
                .param("totalChunks", "5")
                .param("originalFileName", "test.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.url").value("http://localhost:7000/uploads/2026/05/07/test.jpg"));

        verify(chunkUploadService).deleteChunks("abc123");
    }

    @Test
    @DisplayName("测试分片不完整时合并失败")
    void testCompleteUploadIncomplete() throws Exception {
        when(chunkUploadService.isUploadComplete(anyString(), anyInt())).thenReturn(false);

        mockMvc.perform(post("/api/items/upload/complete")
                .param("fileHash", "abc123")
                .param("totalChunks", "5")
                .param("originalFileName", "test.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("分片不完整，请先上传所有分片"));
    }

    @Test
    @DisplayName("测试合并后文件验证失败")
    void testCompleteUploadValidationFailure() throws Exception {
        File tempFile = File.createTempFile("merged", ".exe");
        tempFile.deleteOnExit();

        when(chunkUploadService.isUploadComplete(anyString(), anyInt())).thenReturn(true);
        when(chunkUploadService.mergeChunks(anyString(), anyInt(), anyString())).thenReturn(tempFile);
        doThrow(new IllegalArgumentException("文件类型不支持"))
                .when(fileValidationService).validateImage(any());

        mockMvc.perform(post("/api/items/upload/complete")
                .param("fileHash", "abc123")
                .param("totalChunks", "5")
                .param("originalFileName", "test.exe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("合并后的文件验证失败")));
    }

    @Test
    @DisplayName("测试合并分片时mergeChunks抛出异常")
    void testCompleteUploadMergeException() throws Exception {
        when(chunkUploadService.isUploadComplete(anyString(), anyInt())).thenReturn(true);
        when(chunkUploadService.mergeChunks(anyString(), anyInt(), anyString()))
                .thenThrow(new RuntimeException("合并文件失败"));

        mockMvc.perform(post("/api/items/upload/complete")
                .param("fileHash", "abc123")
                .param("totalChunks", "5")
                .param("originalFileName", "test.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(containsString("合并失败")));
    }
}
