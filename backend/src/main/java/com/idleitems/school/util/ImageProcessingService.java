package com.idleitems.school.util;

import com.idleitems.school.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

    /**
     * 获取最大宽度
     */
    private int getMaxWidth() {
        Integer maxWidth = configService.getConfigInt(CONFIG_MAX_WIDTH);
        return maxWidth != null ? maxWidth : DEFAULT_MAX_WIDTH;
    }

    /**
     * 获取默认质量
     */
    private float getDefaultQuality() {
        Float quality = configService.getConfigFloat(CONFIG_DEFAULT_QUALITY);
        return quality != null ? quality : DEFAULT_QUALITY;
    }

    /**
     * 获取水印透明度
     */
    private float getWatermarkOpacity() {
        Float opacity = configService.getConfigFloat(CONFIG_WATERMARK_OPACITY);
        return opacity != null ? opacity : DEFAULT_WATERMARK_OPACITY;
    }

    /**
     * 获取水印文字
     */
    private String getWatermarkText() {
        String text = configService.getConfigValue(CONFIG_WATERMARK_TEXT);
        return text != null && !text.isEmpty() ? text : DEFAULT_WATERMARK_TEXT;
    }

    /**
     * 处理图片
     * @param inputFile 输入文件
     * @param outputFile 输出文件
     * @param format 输出格式
     * @return 处理后的图片信息
     * @throws IOException 处理异常
     */
    public ImageInfo processImage(File inputFile, File outputFile, String format) throws IOException {
        // 读取图片
        BufferedImage image = ImageIO.read(inputFile);
        
        // 调整尺寸
        int width = image.getWidth();
        int height = image.getHeight();
        
        // 计算新尺寸
        int[] newDimensions = calculateNewDimensions(width, height);
        int newWidth = newDimensions[0];
        int newHeight = newDimensions[1];
        
        // 获取配置值
        float quality = getDefaultQuality();
        float watermarkOpacity = getWatermarkOpacity();
        
        // 处理图片
        Thumbnails.Builder<? extends File> builder = Thumbnails.of(inputFile)
                .size(newWidth, newHeight)
                .outputFormat(format)
                .outputQuality(quality);
        
        // 添加水印
        builder.watermark(Positions.BOTTOM_RIGHT, createWatermark(), watermarkOpacity);
        
        // 保存处理后的图片
        builder.toFile(outputFile);
        
        // 返回图片信息
        return new ImageInfo(
                newWidth,
                newHeight,
                outputFile.length(),
                format
        );
    }

    /**
     * 处理图片
     * @param imageBytes 图片字节数组
     * @param format 输出格式
     * @return 处理后的图片字节数组和信息
     * @throws IOException 处理异常
     */
    public ProcessedImageResult processImage(byte[] imageBytes, String format) throws IOException {
        // 创建输入流
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
        
        // 读取图片
        BufferedImage image = ImageIO.read(inputStream);
        
        // 调整尺寸
        int width = image.getWidth();
        int height = image.getHeight();
        
        // 计算新尺寸
        int[] newDimensions = calculateNewDimensions(width, height);
        int newWidth = newDimensions[0];
        int newHeight = newDimensions[1];
        
        // 获取配置值
        float quality = getDefaultQuality();
        float watermarkOpacity = getWatermarkOpacity();
        
        // 创建输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // 处理图片
        Thumbnails.Builder<? extends InputStream> builder = Thumbnails.of(inputStream)
                .size(newWidth, newHeight)
                .outputFormat(format)
                .outputQuality(quality);
        
        // 添加水印
        builder.watermark(Positions.BOTTOM_RIGHT, createWatermark(), watermarkOpacity);
        
        // 保存处理后的图片
        builder.toOutputStream(outputStream);
        
        // 返回结果
        return new ProcessedImageResult(
                outputStream.toByteArray(),
                new ImageInfo(
                        newWidth,
                        newHeight,
                        outputStream.size(),
                        format
                )
        );
    }

    /**
     * 计算新尺寸
     * @param width 原始宽度
     * @param height 原始高度
     * @return 新尺寸 [width, height]
     */
    private int[] calculateNewDimensions(int width, int height) {
        int maxWidth = getMaxWidth();
        
        if (width <= maxWidth && height <= maxWidth) {
            return new int[]{width, height};
        }
        
        double aspectRatio = (double) width / height;
        int newWidth, newHeight;
        
        if (width > height) {
            newWidth = maxWidth;
            newHeight = (int) (maxWidth / aspectRatio);
        } else {
            newHeight = maxWidth;
            newWidth = (int) (maxWidth * aspectRatio);
        }
        
        return new int[]{newWidth, newHeight};
    }

    /**
     * 创建水印
     * @return 水印图片
     */
    private BufferedImage createWatermark() {
        // 获取水印配置
        float watermarkOpacity = getWatermarkOpacity();
        String watermarkText = getWatermarkText();
        
        // 创建一个简单的文字水印
        int width = 200;
        int height = 50;
        BufferedImage watermark = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = watermark.createGraphics();
        
        // 设置透明度
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, watermarkOpacity));
        
        // 设置字体
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        
        // 设置颜色
        g2d.setColor(Color.WHITE);
        
        // 绘制文字
        FontMetrics metrics = g2d.getFontMetrics();
        int x = (width - metrics.stringWidth(watermarkText)) / 2;
        int y = (height - metrics.getHeight()) / 2 + metrics.getAscent();
        g2d.drawString(watermarkText, x, y);
        
        // 释放资源
        g2d.dispose();
        
        return watermark;
    }

    /**
     * 图片信息类
     */
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

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public long getSize() {
            return size;
        }

        public String getFormat() {
            return format;
        }
    }

    /**
     * 处理图片结果类
     */
    public static class ProcessedImageResult {
        private final byte[] imageBytes;
        private final ImageInfo imageInfo;

        public ProcessedImageResult(byte[] imageBytes, ImageInfo imageInfo) {
            this.imageBytes = imageBytes;
            this.imageInfo = imageInfo;
        }

        public byte[] getImageBytes() {
            return imageBytes;
        }

        public ImageInfo getImageInfo() {
            return imageInfo;
        }
    }
}
