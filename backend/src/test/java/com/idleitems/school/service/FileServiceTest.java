package com.idleitems.school.service;

import com.idleitems.school.service.impl.FileServiceImpl;
import com.idleitems.school.util.FileValidationService;
import com.idleitems.school.util.ImageProcessingService;
import com.idleitems.school.util.storage.StorageServiceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private StorageServiceFactory storageServiceFactory;

    @Mock
    private FileValidationService fileValidationService;

    @Mock
    private ImageProcessingService imageProcessingService;

    @Mock
    private ConfigService configService;

    @InjectMocks
    private FileServiceImpl fileService;

    private Path tempUploadPath;

    @BeforeEach
    void setUp() throws IOException {
        tempUploadPath = Files.createTempDirectory("test_upload");
        ReflectionTestUtils.setField(fileService, "uploadPath", tempUploadPath.toString());
        ReflectionTestUtils.setField(fileService, "baseUrl", null);
    }

    @Test
    void getFileExtension_WithValidExtension_ReturnsExtension() {
        assertEquals(".jpg", fileService.getFileExtension("test.jpg"));
        assertEquals(".png", fileService.getFileExtension("image.png"));
    }

    @Test
    void getFileExtension_WithNoExtension_ReturnsEmpty() {
        assertEquals("", fileService.getFileExtension("noextension"));
        assertEquals("", fileService.getFileExtension(null));
    }

    @Test
    void isAllowedFileType_WithValidType_ReturnsTrue() {
        when(configService.getConfigValue("file_allowed_types")).thenReturn(null);

        assertTrue(fileService.isAllowedFileType("test.jpg"));
        assertTrue(fileService.isAllowedFileType("test.jpeg"));
        assertTrue(fileService.isAllowedFileType("test.png"));
        assertTrue(fileService.isAllowedFileType("test.webp"));
    }

    @Test
    void isAllowedFileType_WithInvalidType_ReturnsFalse() {
        when(configService.getConfigValue("file_allowed_types")).thenReturn(null);

        assertFalse(fileService.isAllowedFileType("test.gif"));
        assertFalse(fileService.isAllowedFileType("test.bmp"));
        assertFalse(fileService.isAllowedFileType(null));
    }

    @Test
    void isAllowedFileType_WithCustomTypes_ReturnsCorrectResult() {
        when(configService.getConfigValue("file_allowed_types")).thenReturn(".gif,.bmp");

        assertTrue(fileService.isAllowedFileType("test.gif"));
        assertTrue(fileService.isAllowedFileType("test.bmp"));
        assertFalse(fileService.isAllowedFileType("test.jpg"));
    }

    @Test
    void getMaxFileSize_WithConfig_ReturnsConfigValue() {
        when(configService.getConfigLong("file_max_size")).thenReturn(1024L * 1024 * 10);

        assertEquals(1024L * 1024 * 10, fileService.getMaxFileSize());
    }

    @Test
    void getMaxFileSize_WithoutConfig_ReturnsDefault() {
        when(configService.getConfigLong("file_max_size")).thenReturn(null);

        assertEquals(5 * 1024 * 1024, fileService.getMaxFileSize());
    }

    @Test
    void getFileUrl_WithoutBaseUrl_ReturnsLocalUrl() {
        ReflectionTestUtils.setField(fileService, "baseUrl", null);

        assertEquals("/uploads/test.jpg", fileService.getFileUrl("test.jpg"));
    }

    @Test
    void getFileUrl_WithBaseUrl_ReturnsFullUrl() {
        ReflectionTestUtils.setField(fileService, "baseUrl", "https://cdn.example.com");

        assertEquals("https://cdn.example.com/test.jpg", fileService.getFileUrl("test.jpg"));
    }

    @Test
    void getFileUrl_WithBaseUrlEndingSlash_ReturnsCorrectUrl() {
        ReflectionTestUtils.setField(fileService, "baseUrl", "https://cdn.example.com/");

        assertEquals("https://cdn.example.com/test.jpg", fileService.getFileUrl("test.jpg"));
    }

    @Test
    void getFile_ReturnsCorrectFile() {
        File result = fileService.getFile("uploads/test.jpg");

        assertNotNull(result);
        assertTrue(result.getAbsolutePath().contains("test.jpg"));
    }

    @Test
    void getFileSize_WhenFileExists_ReturnsSize() throws IOException {
        Path testFilePath = tempUploadPath.resolve("test_size.jpg");
        Files.write(testFilePath, "test content".getBytes());

        long size = fileService.getFileSize("test_size.jpg");

        assertTrue(size > 0);
    }

    @Test
    void getFileSize_WhenFileNotExists_ReturnsZero() {
        long size = fileService.getFileSize("nonexistent.jpg");

        assertEquals(0, size);
    }

    @Test
    void deleteFile_WhenFileExists_ReturnsTrue() throws IOException {
        Path testFile = tempUploadPath.resolve("to_delete.jpg");
        Files.write(testFile, "content".getBytes());

        boolean result = fileService.deleteFile("to_delete.jpg");

        assertTrue(result);
        assertFalse(Files.exists(testFile));
    }

    @Test
    void deleteFile_WhenFileNotExists_ReturnsFalse() {
        boolean result = fileService.deleteFile("nonexistent.jpg");

        assertFalse(result);
    }

    @Test
    void deleteFile_WhenNull_ReturnsFalse() {
        assertFalse(fileService.deleteFile(null));
        assertFalse(fileService.deleteFile(""));
    }

    @Test
    void uploadFile_WhenEmptyFile_ThrowsException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "empty.jpg",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        assertThrows(IllegalArgumentException.class, () -> {
            fileService.uploadFile(emptyFile, "test");
        });
    }

    @Test
    void uploadFile_WhenNullFile_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            fileService.uploadFile(null, "test");
        });
    }

    @Test
    void uploadFile_WhenExceedsMaxSize_ThrowsException() throws IOException {
        when(configService.getConfigLong("file_max_size")).thenReturn(100L);

        MockMultipartFile largeFile = new MockMultipartFile(
                "large.jpg",
                "large.jpg",
                "image/jpeg",
                new byte[200]
        );

        assertThrows(IllegalArgumentException.class, () -> {
            fileService.uploadFile(largeFile, "test");
        });
    }

    @Test
    void validateFiles_WhenNull_ReturnsError() throws IOException {
        List<String> errors = fileService.validateFiles(null);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("请选择文件"));
    }

    @Test
    void validateFiles_WhenEmpty_ReturnsError() throws IOException {
        List<String> errors = fileService.validateFiles(List.of());

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("请选择文件"));
    }

    @Test
    void validateFiles_WhenValidFiles_ReturnsNoErrors() throws IOException {
        MockMultipartFile validFile = new MockMultipartFile(
                "test.jpg",
                "test.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        when(configService.getConfigLong("file_max_size")).thenReturn(null);
        when(configService.getConfigValue("file_allowed_types")).thenReturn(null);

        List<String> errors = fileService.validateFiles(List.of(validFile));

        assertTrue(errors.isEmpty());
    }

    @Test
    void validateFiles_WhenEmptyFile_ReturnsError() throws IOException {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "empty.jpg",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        List<String> errors = fileService.validateFiles(List.of(emptyFile));

        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).contains("文件不能为空"));
    }

    @Test
    void validateFiles_WhenInvalidExtension_ReturnsError() throws IOException {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "test.gif",
                "test.gif",
                "image/gif",
                "content".getBytes()
        );

        when(configService.getConfigLong("file_max_size")).thenReturn(null);
        when(configService.getConfigValue("file_allowed_types")).thenReturn(null);

        List<String> errors = fileService.validateFiles(List.of(invalidFile));

        assertFalse(errors.isEmpty());
    }
}
