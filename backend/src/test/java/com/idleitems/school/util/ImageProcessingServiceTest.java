package com.idleitems.school.util;

import com.idleitems.school.module.system.service.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageProcessingServiceTest {

    @Mock
    private ConfigService configService;

    @InjectMocks
    private ImageProcessingService imageProcessingService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        lenient().when(configService.getConfigInt("file_max_width")).thenReturn(null);
        lenient().when(configService.getConfigFloat("file_default_quality")).thenReturn(null);
        lenient().when(configService.getConfigFloat("file_watermark_opacity")).thenReturn(null);
        lenient().when(configService.getConfigValue("file_watermark_text")).thenReturn(null);
    }

    @Test
    void testProcessImageNoResizeNoRecompress() throws Exception {
        when(configService.getConfigFloat("file_default_quality")).thenReturn(1.0f);

        File input = tempDir.resolve("test.jpg").toFile();
        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(img, "jpg", input);

        File output = tempDir.resolve("output.jpg").toFile();
        ImageProcessingService.ImageInfo info = imageProcessingService.processImage(input, output, "jpg");

        assertEquals(800, info.getWidth());
        assertEquals(600, info.getHeight());
        assertTrue(info.getSize() > 0);
        assertEquals("jpg", info.getFormat());
    }

    @Test
    void testProcessImageResize() throws Exception {
        File input = tempDir.resolve("large.jpg").toFile();
        BufferedImage img = new BufferedImage(3840, 2160, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(img, "jpg", input);

        File output = tempDir.resolve("output.jpg").toFile();
        ImageProcessingService.ImageInfo info = imageProcessingService.processImage(input, output, "jpg");

        assertTrue(info.getWidth() <= 1920);
        assertEquals(1080, info.getHeight());
        assertTrue(info.getSize() > 0);
    }

    @Test
    void testProcessImageWithFormatConversion() throws Exception {
        File input = tempDir.resolve("test.png").toFile();
        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(img, "png", input);

        File output = tempDir.resolve("output.png").toFile();
        ImageProcessingService.ImageInfo info = imageProcessingService.processImage(input, output, "png");

        assertNotNull(info);
        assertTrue(info.getSize() > 0);
        assertEquals("png", info.getFormat());
    }

    @Test
    void testProcessImagePortraitOrientation() throws Exception {
        File input = tempDir.resolve("portrait.jpg").toFile();
        BufferedImage img = new BufferedImage(1080, 1920, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(img, "jpg", input);

        File output = tempDir.resolve("output.jpg").toFile();
        ImageProcessingService.ImageInfo info = imageProcessingService.processImage(input, output, "jpg");

        assertTrue(info.getWidth() <= 1920);
        assertTrue(info.getHeight() <= 1920);
        assertTrue(info.getSize() > 0);
    }

    @Test
    void testProcessImageSquare() throws Exception {
        File input = tempDir.resolve("square.jpg").toFile();
        BufferedImage img = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(img, "jpg", input);

        File output = tempDir.resolve("output.jpg").toFile();
        ImageProcessingService.ImageInfo info = imageProcessingService.processImage(input, output, "jpg");

        assertTrue(info.getWidth() <= 1920);
        assertTrue(info.getHeight() <= 1920);
    }

    @Test
    void testProcessImageSmallDimensions() throws Exception {
        File input = tempDir.resolve("small.jpg").toFile();
        BufferedImage img = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(img, "jpg", input);

        File output = tempDir.resolve("output.jpg").toFile();
        ImageProcessingService.ImageInfo info = imageProcessingService.processImage(input, output, "jpg");

        assertEquals(50, info.getWidth());
        assertEquals(50, info.getHeight());
    }

    @Test
    void testProcessImageBytesWithResize() throws Exception {
        BufferedImage img = new BufferedImage(3840, 2160, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();

        ImageProcessingService.ProcessedImageResult result =
                imageProcessingService.processImage(imageBytes, "jpg");

        assertNotNull(result);
        assertTrue(result.getImageInfo().getWidth() <= 1920);
        assertTrue(result.getImageInfo().getHeight() <= 1920);
        assertTrue(result.getImageBytes().length > 0);
    }

    @Test
    void testProcessImageBytesWithFormatConversion() throws Exception {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        ImageProcessingService.ProcessedImageResult result =
                imageProcessingService.processImage(imageBytes, "png");

        assertNotNull(result);
        assertEquals(100, result.getImageInfo().getWidth());
        assertEquals(100, result.getImageInfo().getHeight());
    }

    @Test
    void testProcessImageBytesNoResizeNoRecompress() throws Exception {
        when(configService.getConfigFloat("file_default_quality")).thenReturn(1.0f);

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();

        ImageProcessingService.ProcessedImageResult result =
                imageProcessingService.processImage(imageBytes, "jpg");

        assertNotNull(result);
        assertEquals(100, result.getImageInfo().getWidth());
        assertEquals(100, result.getImageInfo().getHeight());
    }

    @Test
    void testStripExifUnreadableImage() {
        File input = tempDir.resolve("notanimage.txt").toFile();
        File output = tempDir.resolve("output.jpg").toFile();

        assertThrows(Exception.class, () -> {
            imageProcessingService.processImage(input, output, "jpg");
        });
    }

    @Test
    void testProcessImageWithCustomMaxWidth() throws Exception {
        when(configService.getConfigInt("file_max_width")).thenReturn(500);

        File input = tempDir.resolve("test.jpg").toFile();
        BufferedImage img = new BufferedImage(1000, 800, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(img, "jpg", input);

        File output = tempDir.resolve("output.jpg").toFile();
        ImageProcessingService.ImageInfo info = imageProcessingService.processImage(input, output, "jpg");

        assertTrue(info.getWidth() <= 500);
        assertTrue(info.getHeight() <= 500);
    }
}
