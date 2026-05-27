package com.idleitems.school.util;

import com.idleitems.school.service.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageProcessingServiceTest {

    @Mock
    private ConfigService configService;

    private ImageProcessingService imageProcessingService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(configService.getConfigInt("file_max_width")).thenReturn(null);
        when(configService.getConfigFloat("file_default_quality")).thenReturn(null);
        when(configService.getConfigFloat("file_watermark_opacity")).thenReturn(null);
        when(configService.getConfigValue("file_watermark_text")).thenReturn(null);
        imageProcessingService = new ImageProcessingService(configService);
    }

    @Test
    void testProcessImageNoResizeNeeded() throws Exception {
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
    void testProcessImageWatermarkAdded() throws Exception {
        File input = tempDir.resolve("test.jpg").toFile();
        BufferedImage img = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(img, "jpg", input);

        File output = tempDir.resolve("watermarked.jpg").toFile();
        ImageProcessingService.ImageInfo info = imageProcessingService.processImage(input, output, "jpg");

        assertNotNull(info);
        assertEquals(400, info.getWidth());
        assertEquals(300, info.getHeight());
    }

    @Test
    void testCalculateDimensionsWithinLimit() throws Exception {
        File input = tempDir.resolve("test.jpg").toFile();
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(img, "jpg", input);

        File output = tempDir.resolve("output.jpg").toFile();
        ImageProcessingService.ImageInfo info = imageProcessingService.processImage(input, output, "jpg");

        assertEquals(100, info.getWidth());
        assertEquals(100, info.getHeight());
    }

    @Test
    void testProcessImageBytes() throws Exception {
        BufferedImage img = new BufferedImage(200, 150, BufferedImage.TYPE_INT_RGB);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();

        ImageProcessingService.ProcessedImageResult result =
                imageProcessingService.processImage(imageBytes, "jpg");

        assertNotNull(result);
        assertEquals(200, result.getImageInfo().getWidth());
        assertEquals(150, result.getImageInfo().getHeight());
        assertTrue(result.getImageBytes().length > 0);
    }
}
