package com.idleitems.school.util.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LocalStorageAdapterTest {

    private LocalStorageAdapter adapter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        adapter = new LocalStorageAdapter();
        ReflectionTestUtils.setField(adapter, "baseUploadPath", tempDir.toString() + "/");
        ReflectionTestUtils.setField(adapter, "baseUrl", "http://localhost:7000");
    }

    @Test
    void upload_WithInputStream_ReturnsResult() throws Exception {
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes(StandardCharsets.UTF_8));

        Map<String, Object> result = adapter.upload(inputStream, "testfile.txt", "text/plain");

        assertNotNull(result.get("path"));
        assertNotNull(result.get("url"));
        assertTrue(result.containsKey("size"));
        assertEquals("text/plain", result.get("contentType"));
        assertTrue(((String) result.get("url")).contains("/uploads/"));
    }

    @Test
    void upload_WithFile_ReturnsResult() throws Exception {
        File sourceFile = tempDir.resolve("source.txt").toFile();
        Files.writeString(sourceFile.toPath(), "file content");

        Map<String, Object> result = adapter.upload(sourceFile, "uploaded.txt", "text/plain");

        assertNotNull(result.get("path"));
        assertNotNull(result.get("url"));
        assertTrue(result.containsKey("size"));
        assertEquals("text/plain", result.get("contentType"));
        assertTrue(((String) result.get("url")).contains("/uploads/"));
    }

    @Test
    void delete_ExistingFile_ReturnsTrue() throws Exception {
        Path testFile = tempDir.resolve("delete_test.txt");
        Files.writeString(testFile, "to be deleted");

        Path uploadPath = tempDir.resolve("uploads").resolve("delete_test.txt");
        Files.createDirectories(uploadPath.getParent());
        Files.copy(testFile, uploadPath);

        String relativePath = "uploads/delete_test.txt";
        boolean deleted = adapter.delete(relativePath);

        assertTrue(deleted);
        assertFalse(Files.exists(uploadPath));
    }

    @Test
    void delete_NonExistentFile_ReturnsFalse() throws Exception {
        boolean deleted = adapter.delete("nonexistent.txt");

        assertFalse(deleted);
    }

    @Test
    void getFileUrl_ReturnsCorrectUrl() {
        String url = adapter.getFileUrl("test/path/file.jpg");

        assertEquals("http://localhost:7000/uploads/test/path/file.jpg", url);
    }

    @Test
    void exists_ExistingFile_ReturnsTrue() throws Exception {
        Path testFile = tempDir.resolve("exists_test.txt");
        Files.createDirectories(testFile.getParent());
        Files.writeString(testFile, "exists");

        boolean result = adapter.exists("exists_test.txt");

        assertTrue(result);
    }

    @Test
    void exists_NonExistentFile_ReturnsFalse() {
        boolean result = adapter.exists("does_not_exist.txt");

        assertFalse(result);
    }
}
