package com.idleitems.school.util;

import com.idleitems.school.module.system.service.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileValidationServiceTest {

    @Mock
    private ConfigService configService;

    @InjectMocks
    private FileValidationService fileValidationService;

    private byte[] jpegMagicBytes;
    private byte[] pngMagicBytes;

    @BeforeEach
    void setUp() {
        jpegMagicBytes = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46};
        pngMagicBytes = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        lenient().when(configService.getConfigLong("file_max_size")).thenReturn(null);
        lenient().when(configService.getConfigValue("file_allowed_types")).thenReturn(null);
        lenient().when(configService.getConfigValue("file_allowed_content_types")).thenReturn(null);
    }

    @Test
    void validateImage_NormalJpg_ReturnsResult() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream(jpegMagicBytes))
                .thenReturn(new ByteArrayInputStream(jpegMagicBytes));

        BufferedImage mockImage = mock(BufferedImage.class);
        when(mockImage.getWidth()).thenReturn(800);
        when(mockImage.getHeight()).thenReturn(600);

        try (var mockedImageIO = mockStatic(ImageIO.class)) {
            mockedImageIO.when(() -> ImageIO.read(any(InputStream.class))).thenReturn(mockImage);

            FileValidationService.ImageValidationResult result = fileValidationService.validateImage(file);

            assertEquals(800, result.getWidth());
            assertEquals(600, result.getHeight());
            assertEquals("jpg", result.getExtension());
        }
    }

    @Test
    void validateImage_NormalPng_ReturnsResult() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(2048L);
        when(file.getOriginalFilename()).thenReturn("test.png");
        when(file.getContentType()).thenReturn("image/png");
        when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream(pngMagicBytes))
                .thenReturn(new ByteArrayInputStream(pngMagicBytes));

        BufferedImage mockImage = mock(BufferedImage.class);
        when(mockImage.getWidth()).thenReturn(400);
        when(mockImage.getHeight()).thenReturn(300);

        try (var mockedImageIO = mockStatic(ImageIO.class)) {
            mockedImageIO.when(() -> ImageIO.read(any(InputStream.class))).thenReturn(mockImage);

            FileValidationService.ImageValidationResult result = fileValidationService.validateImage(file);

            assertEquals(400, result.getWidth());
            assertEquals(300, result.getHeight());
            assertEquals("png", result.getExtension());
        }
    }

    @Test
    void validateImage_EmptyFile_ThrowsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileValidationService.validateImage(file));
        assertEquals("文件不能为空", ex.getMessage());
    }

    @Test
    void validateImage_ExceedsMaxSize_ThrowsException() {
        when(configService.getConfigLong("file_max_size")).thenReturn(100L);

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(200L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileValidationService.validateImage(file));
        assertTrue(ex.getMessage().contains("文件大小不能超过"));
    }

    @Test
    void validateImage_NullFilename_ThrowsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getOriginalFilename()).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileValidationService.validateImage(file));
        assertEquals("文件名不能为空", ex.getMessage());
    }

    @Test
    void validateImage_PathTraversal_ThrowsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getOriginalFilename()).thenReturn("../etc/passwd");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileValidationService.validateImage(file));
        assertEquals("文件名包含非法字符", ex.getMessage());
    }

    @Test
    void validateImage_InvalidExtension_ThrowsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getOriginalFilename()).thenReturn("test.gif");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileValidationService.validateImage(file));
        assertTrue(ex.getMessage().contains("文件类型不支持"));
    }

    @Test
    void validateImage_InvalidContentType_ThrowsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getContentType()).thenReturn("image/gif");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileValidationService.validateImage(file));
        assertTrue(ex.getMessage().contains("文件类型不支持"));
    }

    @Test
    void validateImage_MagicByteMismatch_ThrowsException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");

        byte[] fakeHeader = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07};
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(fakeHeader));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileValidationService.validateImage(file));
        assertTrue(ex.getMessage().contains("文件内容与声明的类型不匹配"));
    }

    @Test
    void validateImage_InvalidImageContent_ThrowsException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream(jpegMagicBytes))
                .thenReturn(new ByteArrayInputStream(jpegMagicBytes));

        try (var mockedImageIO = mockStatic(ImageIO.class)) {
            mockedImageIO.when(() -> ImageIO.read(any(InputStream.class))).thenReturn(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> fileValidationService.validateImage(file));
            assertEquals("无效的图片文件", ex.getMessage());
        }
    }

    @Test
    void validateVideo_NormalMp4_ReturnsResult() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L * 1024L);
        when(file.getOriginalFilename()).thenReturn("video.mp4");
        when(file.getContentType()).thenReturn("video/mp4");

        FileValidationService.VideoValidationResult result = fileValidationService.validateVideo(file);

        assertEquals("mp4", result.getExtension());
    }

    @Test
    void validateVideo_EmptyFile_ThrowsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileValidationService.validateVideo(file));
        assertEquals("文件不能为空", ex.getMessage());
    }

    @Test
    void validateVideo_ExceedsMaxSize_ThrowsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(200L * 1024L * 1024L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileValidationService.validateVideo(file));
        assertTrue(ex.getMessage().contains("视频文件大小不能超过"));
    }

    @Test
    void validateVideo_InvalidExtension_ThrowsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("video.exe");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileValidationService.validateVideo(file));
        assertTrue(ex.getMessage().contains("视频格式不支持"));
    }

    @Test
    void validateVideo_InvalidContentType_ThrowsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("video.mp4");
        when(file.getContentType()).thenReturn("application/pdf");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileValidationService.validateVideo(file));
        assertEquals("视频文件类型不匹配", ex.getMessage());
    }

    @Test
    void validateFileSize_Normal_DoesNotThrow() {
        assertDoesNotThrow(() -> fileValidationService.validateFileSize(100L));
    }

    @Test
    void validateFileSize_Exceeds_ThrowsException() {
        when(configService.getConfigLong("file_max_size")).thenReturn(100L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileValidationService.validateFileSize(200L));
        assertTrue(ex.getMessage().contains("文件大小不能超过"));
    }

    @Test
    void validateFileType_Valid_DoesNotThrow() {
        assertDoesNotThrow(() -> fileValidationService.validateFileType("jpg", "image/jpeg"));
    }

    @Test
    void validateFileType_InvalidExtension_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileValidationService.validateFileType("gif", "image/jpeg"));
        assertTrue(ex.getMessage().contains("文件类型不支持"));
    }

    @Test
    void validateFileType_InvalidContentType_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileValidationService.validateFileType("jpg", "image/gif"));
        assertTrue(ex.getMessage().contains("文件类型不支持"));
    }

    @Test
    void getFileExtension_WithDot_ReturnsExtension() {
        String ext = fileValidationService.getFileExtension("document.pdf");
        assertEquals("pdf", ext);
    }

    @Test
    void getFileExtension_NoDot_ReturnsEmpty() {
        String ext = fileValidationService.getFileExtension("document");
        assertEquals("", ext);
    }
}
