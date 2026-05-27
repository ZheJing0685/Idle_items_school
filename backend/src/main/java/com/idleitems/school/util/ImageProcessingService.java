package com.idleitems.school.util;

import com.idleitems.school.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageProcessingService {

    private final ConfigService configService;

    private static final String CONFIG_MAX_WIDTH = "file_max_width";
    private static final String CONFIG_DEFAULT_QUALITY = "file_default_quality";
    private static final String CONFIG_WATERMARK_OPACITY = "file_watermark_opacity";
    private static final String CONFIG_WATERMARK_TEXT = "file_watermark_text";

    private static final int DEFAULT_MAX_WIDTH = 1920;
    private static final float DEFAULT_QUALITY = 0.8f;
    private static final float DEFAULT_WATERMARK_OPACITY = 0.3f;
    private static final String DEFAULT_WATERMARK_TEXT = "Idle Items School";

    private static final int MIN_DIMENSION = 50;

    private BufferedImage cachedWatermark;
    private String cachedWatermarkText;

    private int getMaxWidth() {
        Integer maxWidth = configService.getConfigInt(CONFIG_MAX_WIDTH);
        return maxWidth != null ? maxWidth : DEFAULT_MAX_WIDTH;
    }

    private float getDefaultQuality() {
        Float quality = configService.getConfigFloat(CONFIG_DEFAULT_QUALITY);
        return quality != null ? quality : DEFAULT_QUALITY;
    }

    private float getWatermarkOpacity() {
        Float opacity = configService.getConfigFloat(CONFIG_WATERMARK_OPACITY);
        return opacity != null ? opacity : DEFAULT_WATERMARK_OPACITY;
    }

    private String getWatermarkText() {
        String text = configService.getConfigValue(CONFIG_WATERMARK_TEXT);
        return text != null && !text.isEmpty() ? text : DEFAULT_WATERMARK_TEXT;
    }

    public ImageInfo processImage(File inputFile, File outputFile, String format) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);
        return processImage(inputFile, outputFile, format, image.getWidth(), image.getHeight());
    }

    /**
     * 处理图片：缩放 + 水印 + EXIF清理
     * 通过重新编码图片，自动清除EXIF等元数据（GPS定位、设备信息等隐私数据）
     */
    public ImageInfo processImage(File inputFile, File outputFile, String format,
                                   int knownWidth, int knownHeight) throws IOException {
        int maxWidth = getMaxWidth();

        boolean needsResize = knownWidth > maxWidth || knownHeight > maxWidth;
        boolean needsFormatConversion = !"jpg".equalsIgnoreCase(format);
        float quality = getDefaultQuality();
        boolean needsRecompress = needsFormatConversion || quality < 1.0f;

        if (!needsResize && !needsRecompress) {
            // 即使不需要缩放，也要重新编码以清除EXIF
            stripExifMetadata(inputFile, outputFile, format);
            return new ImageInfo(knownWidth, knownHeight, outputFile.length(), format);
        }

        int[] newDimensions = calculateNewDimensions(knownWidth, knownHeight);
        int newWidth = newDimensions[0];
        int newHeight = newDimensions[1];

        float watermarkOpacity = getWatermarkOpacity();

        Thumbnails.Builder<? extends File> builder = Thumbnails.of(inputFile)
                .size(newWidth, newHeight)
                .outputFormat(format)
                .outputQuality(quality)
                .keepAspectRatio(true);

        builder.watermark(Positions.BOTTOM_RIGHT, getOrCreateWatermark(), watermarkOpacity);
        builder.toFile(outputFile);

        return new ImageInfo(newWidth, newHeight, outputFile.length(), format);
    }

    /**
     * 清除图片EXIF元数据
     * 通过重新编码图片，移除GPS定位、拍摄设备、时间等隐私信息
     */
    private void stripExifMetadata(File inputFile, File outputFile, String format) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);
        if (image == null) {
            throw new IOException("无法读取图片文件: " + inputFile.getAbsolutePath());
        }

        String imageFormat = normalizeFormat(format);
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
            ImageWriter writer = ImageIO.getImageWritersByFormatName(imageFormat).next();

            // JPEG输出时应用压缩质量
            if ("jpg".equalsIgnoreCase(imageFormat) || "jpeg".equalsIgnoreCase(imageFormat)) {
                ImageWriteParam param = writer.getDefaultWriteParam();
                if (param.canWriteCompressed()) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(getDefaultQuality());
                }
                writer.setOutput(ios);
                writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
            } else {
                writer.setOutput(ios);
                writer.write(image);
            }
            writer.dispose();
        }
        log.debug("EXIF元数据已清除: {} -> {}", inputFile.getName(), outputFile.getName());
    }

    /**
     * 规范化图片格式名称
     */
    private String normalizeFormat(String format) {
        if (format == null) return "jpg";
        String lower = format.toLowerCase();
        if ("jpeg".equals(lower)) return "jpg";
        return lower;
    }

    public ProcessedImageResult processImage(byte[] imageBytes, String format) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

        int width = image.getWidth();
        int height = image.getHeight();
        int maxWidth = getMaxWidth();

        boolean needsResize = width > maxWidth || height > maxWidth;
        float quality = getDefaultQuality();
        boolean needsRecompress = quality < 1.0f || !"jpg".equalsIgnoreCase(format);

        if (!needsResize && !needsRecompress) {
            return new ProcessedImageResult(
                    imageBytes,
                    new ImageInfo(width, height, imageBytes.length, format)
            );
        }

        int[] newDimensions = calculateNewDimensions(width, height);
        int newWidth = newDimensions[0];
        int newHeight = newDimensions[1];
        float watermarkOpacity = getWatermarkOpacity();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.Builder<? extends InputStream> builder = Thumbnails.of(new ByteArrayInputStream(imageBytes))
                .size(newWidth, newHeight)
                .outputFormat(format)
                .outputQuality(quality);

        builder.watermark(Positions.BOTTOM_RIGHT, getOrCreateWatermark(), watermarkOpacity);
        builder.toOutputStream(outputStream);

        return new ProcessedImageResult(
                outputStream.toByteArray(),
                new ImageInfo(newWidth, newHeight, outputStream.size(), format)
        );
    }

    private int[] calculateNewDimensions(int width, int height) {
        int maxWidth = getMaxWidth();

        if (width <= maxWidth && height <= maxWidth) {
            return new int[]{Math.max(width, MIN_DIMENSION), Math.max(height, MIN_DIMENSION)};
        }

        double aspectRatio = (double) width / height;

        if (aspectRatio > 10.0 || aspectRatio < 0.1) {
            log.warn("异常宽高比 {}/{} = {}, 强制限制", width, height, aspectRatio);
        }

        int newWidth, newHeight;
        if (width > height) {
            newWidth = maxWidth;
            newHeight = Math.max((int) (maxWidth / aspectRatio), MIN_DIMENSION);
        } else {
            newHeight = maxWidth;
            newWidth = Math.max((int) (maxWidth * aspectRatio), MIN_DIMENSION);
        }

        return new int[]{newWidth, newHeight};
    }

    private BufferedImage getOrCreateWatermark() {
        String currentText = getWatermarkText();
        if (cachedWatermark != null && cachedWatermarkText != null && cachedWatermarkText.equals(currentText)) {
            return cachedWatermark;
        }
        cachedWatermark = createWatermarkImage(currentText);
        cachedWatermarkText = currentText;
        return cachedWatermark;
    }

    private BufferedImage createWatermarkImage(String text) {
        int width = 200;
        int height = 50;
        BufferedImage watermark = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = watermark.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(Color.WHITE);

        FontMetrics metrics = g2d.getFontMetrics();
        int x = (width - metrics.stringWidth(text)) / 2;
        int y = (height - metrics.getHeight()) / 2 + metrics.getAscent();
        g2d.drawString(text, x, y);
        g2d.dispose();

        return watermark;
    }

    public static class ImageInfo {
        private final int width;
        private final int height;
        private final long size;
        private final String format;

        public ImageInfo(int width, int height, long size, String format) {
            this.width = width;
            this.height = height;
            this.size = size;
            this.format = format;
        }

        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public long getSize() { return size; }
        public String getFormat() { return format; }
    }

    public static class ProcessedImageResult {
        private final byte[] imageBytes;
        private final ImageInfo imageInfo;

        public ProcessedImageResult(byte[] imageBytes, ImageInfo imageInfo) {
            this.imageBytes = imageBytes;
            this.imageInfo = imageInfo;
        }

        public byte[] getImageBytes() { return imageBytes; }
        public ImageInfo getImageInfo() { return imageInfo; }
    }
}
