package com.idleitems.school.controller;

import com.idleitems.school.module.file.controller.FileController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileController 文件服务测试")
class FileControllerTest {

    @TempDir
    Path tempDir;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FileController controller = new FileController();
        ReflectionTestUtils.setField(controller, "uploadPath", tempDir.toString());
        mockMvc = standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("下载存在的文件成功")
    void serveFile_found_returnsFile() throws Exception {
        Path subDir = Files.createDirectories(tempDir.resolve("images"));
        Files.writeString(subDir.resolve("test.jpg"), "image-content");

        mockMvc.perform(get("/uploads/images/test.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().bytes("image-content".getBytes()))
                .andExpect(header().string("Cache-Control", "private, no-cache, max-age=0"));
    }

    @Test
    @DisplayName("下载不存在的文件返回404")
    void serveFile_notFound_returns404() throws Exception {
        mockMvc.perform(get("/uploads/missing.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("请求verification路径返回403")
    void serveFile_verificationPath_returns403() throws Exception {
        mockMvc.perform(get("/uploads/verification/doc.jpg"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("路径遍历攻击返回403")
    void serveFile_pathTraversal_returns403() throws Exception {
        mockMvc.perform(get("/uploads/../etc/passwd"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("路径遍历攻击带编码返回403或500")
    void serveFile_pathTraversalEncoded_returns403() throws Exception {
        mockMvc.perform(get("/uploads/..%2F..%2Fsecret.txt"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("请求目录返回404")
    void serveFile_directory_returns404() throws Exception {
        Files.createDirectories(tempDir.resolve("somedir"));

        mockMvc.perform(get("/uploads/somedir"))
                .andExpect(status().isNotFound());
    }
}
