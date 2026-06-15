package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.module.file.service.impl.FileServiceImpl;
import com.idleitems.school.module.system.service.ConfigService;
import com.idleitems.school.util.FileValidationService;
import com.idleitems.school.util.ImageProcessingService;
import com.idleitems.school.util.storage.StorageAdapter;
import com.idleitems.school.util.storage.StorageServiceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Mock
    private StorageAdapter storageAdapter;

    @InjectMocks
    private FileServiceImpl fileService;

    private Path tempUploadPath;

    @BeforeEach
    void setUp() throws IOException {
        tempUploadPath = Files.createTempDirectory("test_upload");
        ReflectionTestUtils.setField(fileService, "uploadPath", tempUploadPath.toString());
        ReflectionTestUtils.setField(fileService, "baseUrl", null);
    }

    private byte[] createValidJpegBytes() throws IOException {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.fillRect(0, 0, 1, 1);
        g2d.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        return baos.toByteArray();
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
    void deleteFile_WhenPathIsNull_ReturnsFalse() {
        assertFalse(fileService.deleteFile(null));
        assertFalse(fileService.deleteFile(""));
    }

    @Test
    void uploadFile_Success_ReturnsPath() throws IOException {
        byte[] jpegBytes = createValidJpegBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", jpegBytes
        );

        when(configService.getConfigLong("file_max_size")).thenReturn(null);
        when(configService.getConfigValue("file_allowed_types")).thenReturn(null);

        String result = fileService.uploadFile(file, "test-dir");

        assertNotNull(result);
        assertTrue(result.startsWith("test-dir/"));
        assertTrue(result.endsWith(".jpg"));
        Path savedPath = tempUploadPath.resolve(result);
        assertTrue(Files.exists(savedPath));
    }

    @Test
    void uploadFile_WhenEmptyFile_ThrowsException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]
        );

        assertThrows(BusinessException.class, () ->
                fileService.uploadFile(emptyFile, "test"));
    }

    @Test
    void uploadFile_WhenNullFile_ThrowsException() {
        assertThrows(BusinessException.class, () ->
                fileService.uploadFile(null, "test"));
    }

    @Test
    void uploadFile_WhenExceedsMaxSize_ThrowsException() {
        when(configService.getConfigLong("file_max_size")).thenReturn(100L);

        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large.jpg", "image/jpeg", new byte[200]
        );

        assertThrows(BusinessException.class, () ->
                fileService.uploadFile(largeFile, "test"));
    }

    @Test
    void uploadFile_WhenInvalidExtension_ThrowsException() {
        when(configService.getConfigLong("file_max_size")).thenReturn(null);
        when(configService.getConfigValue("file_allowed_types")).thenReturn(null);

        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "test.gif", "image/gif", "content".getBytes()
        );

        assertThrows(BusinessException.class, () ->
                fileService.uploadFile(invalidFile, "test"));
    }

    @Test
    void uploadFile_WhenNullOriginalFilename_ThrowsException() {
        when(configService.getConfigLong("file_max_size")).thenReturn(null);

        MockMultipartFile file = new MockMultipartFile(
                "file", (String) null, "image/jpeg", "content".getBytes()
        );

        assertThrows(BusinessException.class, () ->
                fileService.uploadFile(file, "test"));
    }

    @Test
    void uploadImage_Success_ReturnsImageInfo() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", createValidJpegBytes()
        );

        FileValidationService.ImageValidationResult validationResult =
                new FileValidationService.ImageValidationResult(800, 600, "jpg");
        when(fileValidationService.validateImage(file)).thenReturn(validationResult);

        when(storageServiceFactory.getStorageAdapter()).thenReturn(storageAdapter);

        ImageProcessingService.ImageInfo imageInfo =
                new ImageProcessingService.ImageInfo(800, 600, 102400, "jpg");
        when(imageProcessingService.processImage(any(File.class), any(File.class), eq("jpg"), eq(800), eq(600)))
                .thenReturn(imageInfo);

        Map<String, Object> storageResult = new HashMap<>();
        storageResult.put("url", "https://cdn.example.com/test.jpg");
        storageResult.put("path", "uploads/test.jpg");
        when(storageAdapter.upload(any(File.class), anyString(), anyString()))
                .thenReturn(storageResult);

        Map<String, Object> result = fileService.uploadImage(file);

        assertNotNull(result);
        assertEquals("https://cdn.example.com/test.jpg", result.get("url"));
        assertEquals("uploads/test.jpg", result.get("path"));
        assertEquals(800, result.get("width"));
        assertEquals(600, result.get("height"));
        assertEquals(102400L, result.get("size"));
        assertEquals("jpg", result.get("format"));
        assertTrue(((String) result.get("fileName")).endsWith(".jpg"));
        assertEquals("test.jpg", result.get("originalName"));
    }

    @Test
    void uploadImage_WhenOriginalFilenameBlank_UsesUnknown() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "", "image/jpeg", createValidJpegBytes()
        );

        FileValidationService.ImageValidationResult validationResult =
                new FileValidationService.ImageValidationResult(100, 100, "jpg");
        when(fileValidationService.validateImage(file)).thenReturn(validationResult);
        when(storageServiceFactory.getStorageAdapter()).thenReturn(storageAdapter);

        ImageProcessingService.ImageInfo imageInfo =
                new ImageProcessingService.ImageInfo(100, 100, 1024, "jpg");
        when(imageProcessingService.processImage(any(File.class), any(File.class), eq("jpg"), eq(100), eq(100)))
                .thenReturn(imageInfo);

        Map<String, Object> storageResult = new HashMap<>();
        storageResult.put("url", "https://cdn.example.com/file.jpg");
        storageResult.put("path", "uploads/file.jpg");
        when(storageAdapter.upload(any(File.class), anyString(), anyString()))
                .thenReturn(storageResult);

        Map<String, Object> result = fileService.uploadImage(file);

        assertNotNull(result);
        assertEquals("unknown", result.get("originalName"));
    }

    @Test
    void uploadChatMedia_WhenVideo_ReturnsVideoInfo() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "video.mp4", "video/mp4", "fake video content".getBytes()
        );

        FileValidationService.VideoValidationResult validationResult =
                new FileValidationService.VideoValidationResult("mp4");
        when(fileValidationService.validateVideo(file)).thenReturn(validationResult);
        when(storageServiceFactory.getStorageAdapter()).thenReturn(storageAdapter);

        Map<String, Object> storageResult = new HashMap<>();
        storageResult.put("url", "https://cdn.example.com/video.mp4");
        storageResult.put("path", "uploads/video.mp4");
        when(storageAdapter.upload(any(File.class), anyString(), anyString()))
                .thenReturn(storageResult);

        Map<String, Object> result = fileService.uploadChatMedia(file);

        assertNotNull(result);
        assertEquals("https://cdn.example.com/video.mp4", result.get("url"));
        assertEquals("uploads/video.mp4", result.get("path"));
        assertEquals("video", result.get("mediaType"));
        assertTrue(((String) result.get("fileName")).endsWith(".mp4"));
        assertEquals("video.mp4", result.get("originalName"));
    }

    @Test
    void uploadChatMedia_WhenImage_DelegatesToUploadImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", createValidJpegBytes()
        );

        FileValidationService.ImageValidationResult validationResult =
                new FileValidationService.ImageValidationResult(400, 300, "jpg");
        when(fileValidationService.validateImage(file)).thenReturn(validationResult);
        when(storageServiceFactory.getStorageAdapter()).thenReturn(storageAdapter);

        ImageProcessingService.ImageInfo imageInfo =
                new ImageProcessingService.ImageInfo(400, 300, 2048, "jpg");
        when(imageProcessingService.processImage(any(File.class), any(File.class), eq("jpg"), eq(400), eq(300)))
                .thenReturn(imageInfo);

        Map<String, Object> storageResult = new HashMap<>();
        storageResult.put("url", "https://cdn.example.com/photo.jpg");
        storageResult.put("path", "uploads/photo.jpg");
        when(storageAdapter.upload(any(File.class), anyString(), anyString()))
                .thenReturn(storageResult);

        Map<String, Object> result = fileService.uploadChatMedia(file);

        assertNotNull(result);
        assertEquals("https://cdn.example.com/photo.jpg", result.get("url"));
    }

    @Test
    void uploadChatMedia_WhenVideoAndOriginalFilenameBlank_UsesUnknown() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "", "video/mp4", "content".getBytes()
        );

        FileValidationService.VideoValidationResult validationResult =
                new FileValidationService.VideoValidationResult("mp4");
        when(fileValidationService.validateVideo(file)).thenReturn(validationResult);
        when(storageServiceFactory.getStorageAdapter()).thenReturn(storageAdapter);

        Map<String, Object> storageResult = new HashMap<>();
        storageResult.put("url", "https://cdn.example.com/vid.mp4");
        storageResult.put("path", "uploads/vid.mp4");
        when(storageAdapter.upload(any(File.class), anyString(), anyString()))
                .thenReturn(storageResult);

        Map<String, Object> result = fileService.uploadChatMedia(file);

        assertNotNull(result);
        assertEquals("unknown", result.get("originalName"));
    }

    @Test
    void validateFiles_WhenNull_ReturnsError() throws IOException {
        List<String> errors = fileService.validateFiles(null);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("请选择文件"));
    }

    @Test
    void validateFiles_WhenEmptyList_ReturnsError() throws IOException {
        List<String> errors = fileService.validateFiles(List.of());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("请选择文件"));
    }

    @Test
    void validateFiles_WhenAllValid_ReturnsNoErrors() throws IOException {
        MockMultipartFile validFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "content".getBytes()
        );

        when(configService.getConfigLong("file_max_size")).thenReturn(null);
        when(configService.getConfigValue("file_allowed_types")).thenReturn(null);

        List<String> errors = fileService.validateFiles(List.of(validFile));
        assertTrue(errors.isEmpty());
    }

    @Test
    void validateFiles_WhenEmptyFile_ReturnsError() throws IOException {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]
        );

        List<String> errors = fileService.validateFiles(List.of(emptyFile));
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).contains("文件不能为空"));
    }

    @Test
    void validateFiles_WhenExceedsMaxSize_ReturnsError() throws IOException {
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large.jpg", "image/jpeg", new byte[1000]
        );

        when(configService.getConfigLong("file_max_size")).thenReturn(100L);

        List<String> errors = fileService.validateFiles(List.of(largeFile));
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).contains("大小"));
    }

    @Test
    void validateFiles_WhenInvalidExtension_ReturnsError() throws IOException {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "test.gif", "image/gif", "content".getBytes()
        );

        when(configService.getConfigLong("file_max_size")).thenReturn(null);
        when(configService.getConfigValue("file_allowed_types")).thenReturn(null);

        List<String> errors = fileService.validateFiles(List.of(invalidFile));
        assertFalse(errors.isEmpty());
    }

    @Test
    void validateFiles_WhenMultipleFiles_ReturnsMultipleErrors() throws IOException {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]
        );
        MockMultipartFile invalidExtFile = new MockMultipartFile(
                "file", "test.gif", "image/gif", "content".getBytes()
        );

        when(configService.getConfigLong("file_max_size")).thenReturn(null);
        when(configService.getConfigValue("file_allowed_types")).thenReturn(null);

        List<String> errors = fileService.validateFiles(List.of(emptyFile, invalidExtFile));
        assertEquals(2, errors.size());
    }
}
