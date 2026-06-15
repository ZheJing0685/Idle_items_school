package com.idleitems.school.service;

import com.idleitems.school.module.file.service.ChunkUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChunkUploadService 分片上传服务测试")
class ChunkUploadServiceTest {

    @TempDir
    Path tempDir;

    private ChunkUploadService chunkUploadService;

    @BeforeEach
    void setUp() {
        chunkUploadService = new ChunkUploadService();
        ReflectionTestUtils.setField(chunkUploadService, "uploadPath", tempDir.toString());
        ReflectionTestUtils.setField(chunkUploadService, "chunkSize", 1048576L);
    }

    @Test
    @DisplayName("获取分片目录路径")
    void getChunkDir_returnsCorrectPath() {
        String dir = chunkUploadService.getChunkDir("hash123");
        assertEquals(tempDir.toString() + "/chunks/hash123", dir);
    }

    @Test
    @DisplayName("获取分片文件路径")
    void getChunkPath_returnsCorrectPath() {
        Path path = chunkUploadService.getChunkPath("hash123", 0);
        assertEquals(Paths.get(tempDir.toString(), "chunks", "hash123", "0"), path);
    }

    @Test
    @DisplayName("保存单个分片并验证")
    void saveChunk_createsChunkFile() throws IOException {
        MockMultipartFile chunk = new MockMultipartFile("chunk", "chunk0", "application/octet-stream", "test data".getBytes());

        chunkUploadService.saveChunk("hash1", 0, chunk);

        Path expectedPath = Paths.get(tempDir.toString(), "chunks", "hash1", "0");
        assertTrue(Files.exists(expectedPath));
        assertEquals("test data", Files.readString(expectedPath));
    }

    @Test
    @DisplayName("保存多个分片后列出已上传分片")
    void getUploadedChunks_returnsSortedChunks() throws IOException {
        MockMultipartFile chunk = new MockMultipartFile("chunk", "chunk", "application/octet-stream", "data".getBytes());

        chunkUploadService.saveChunk("hash2", 5, chunk);
        chunkUploadService.saveChunk("hash2", 2, chunk);
        chunkUploadService.saveChunk("hash2", 8, chunk);

        List<Integer> uploaded = chunkUploadService.getUploadedChunks("hash2");
        assertEquals(List.of(2, 5, 8), uploaded);
    }

    @Test
    @DisplayName("获取不存在的分片目录返回空列表")
    void getUploadedChunks_dirNotExists_returnsEmptyList() throws IOException {
        List<Integer> uploaded = chunkUploadService.getUploadedChunks("nonexistent");
        assertTrue(uploaded.isEmpty());
    }

    @Test
    @DisplayName("判断分片上传完成")
    void isUploadComplete_returnsTrueWhenAllChunksUploaded() throws IOException {
        MockMultipartFile chunk = new MockMultipartFile("chunk", "chunk", "application/octet-stream", "data".getBytes());
        chunkUploadService.saveChunk("hash3", 0, chunk);
        chunkUploadService.saveChunk("hash3", 1, chunk);
        chunkUploadService.saveChunk("hash3", 2, chunk);

        assertTrue(chunkUploadService.isUploadComplete("hash3", 3));
    }

    @Test
    @DisplayName("判断分片上传不完整")
    void isUploadComplete_returnsFalseWhenMissingChunks() throws IOException {
        MockMultipartFile chunk = new MockMultipartFile("chunk", "chunk", "application/octet-stream", "data".getBytes());
        chunkUploadService.saveChunk("hash4", 0, chunk);

        assertFalse(chunkUploadService.isUploadComplete("hash4", 5));
    }

    @Test
    @DisplayName("无分片时返回未完成")
    void isUploadComplete_noChunks_returnsFalse() throws IOException {
        assertFalse(chunkUploadService.isUploadComplete("empty", 1));
    }

    @Test
    @DisplayName("合并分片成功并删除临时目录")
    void mergeChunks_completesAndCleansUp() throws IOException {
        MockMultipartFile chunk = new MockMultipartFile("chunk", "chunk", "application/octet-stream", "hello".getBytes());
        chunkUploadService.saveChunk("hash5", 0, chunk);
        chunkUploadService.saveChunk("hash5", 1, chunk);

        File merged = chunkUploadService.mergeChunks("hash5", 2, "test.jpg");

        assertTrue(merged.exists());
        assertEquals("hellohello", Files.readString(merged.toPath()));
        assertTrue(Files.notExists(chunkUploadService.getChunkPath("hash5", 0)));
        assertTrue(Files.notExists(chunkUploadService.getChunkPath("hash5", 1)));

        merged.delete();
    }

    @Test
    @DisplayName("合并时缺少分片抛出异常")
    void mergeChunks_missingChunk_throwsIOException() {
        IOException ex = assertThrows(IOException.class,
            () -> chunkUploadService.mergeChunks("hash6", 3, "test.jpg"));
        assertTrue(ex.getMessage().contains("分片"));
    }

    @Test
    @DisplayName("删除分片目录")
    void deleteChunks_removesChunkDirectory() throws IOException {
        MockMultipartFile chunk = new MockMultipartFile("chunk", "chunk", "application/octet-stream", "data".getBytes());
        chunkUploadService.saveChunk("hash7", 0, chunk);

        chunkUploadService.deleteChunks("hash7");

        assertTrue(Files.notExists(Paths.get(tempDir.toString(), "chunks", "hash7")));
    }

    @Test
    @DisplayName("删除不存在的分片目录不报错")
    void deleteChunks_dirNotExists_noError() throws IOException {
        chunkUploadService.deleteChunks("nonexistent");
    }
}
