package com.idleitems.school.util;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片处理服务
 * 处理图片的压缩、尺寸调整、水印添加等操作
 */
public class ImageProcessingService {

    private static final int MAX_WIDTH = 1920;
    private static final float DEFAULT_QUALITY = 0.8f;
    private static final float WATERMARK_OPACITY = 0.3f;

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
        
        // 处理图片
        Thumbnails.Builder<? extends File> builder = Thumbnails.of(inputFile)
                .size(newWidth, newHeight)
                .outputFormat(format)
                .outputQuality(DEFAULT_QUALITY);
        
        // 添加水印
        builder.watermark(Positions.BOTTOM_RIGHT, createWatermark(), WATERMARK_OPACITY);
        
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
        
        // 创建输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // 处理图片
        Thumbnails.Builder<? extends InputStream> builder = Thumbnails.of(inputStream)
                .size(newWidth, newHeight)
                .outputFormat(format)
                .outputQuality(DEFAULT_QUALITY);
        
        // 添加水印
        builder.watermark(Positions.BOTTOM_RIGHT, createWatermark(), WATERMARK_OPACITY);
        
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
        if (width <= MAX_WIDTH && height <= MAX_WIDTH) {
            return new int[]{width, height};
        }
        
        double aspectRatio = (double) width / height;
        int newWidth, newHeight;
        
        if (width > height) {
            newWidth = MAX_WIDTH;
            newHeight = (int) (MAX_WIDTH / aspectRatio);
        } else {
            newHeight = MAX_WIDTH;
            newWidth = (int) (MAX_WIDTH * aspectRatio);
        }
        
        return new int[]{newWidth, newHeight};
    }

    /**
     * 创建水印
     * @return 水印图片
     */
    private BufferedImage createWatermark() {
        // 创建一个简单的文字水印
        int width = 200;
        int height = 50;
        BufferedImage watermark = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = watermark.createGraphics();
        
        // 设置透明度
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, WATERMARK_OPACITY));
        
        // 设置字体
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        
        // 设置颜色
        g2d.setColor(Color.WHITE);
        
        // 绘制文字
        String text = "Idle Items School";
        FontMetrics metrics = g2d.getFontMetrics();
        int x = (width - metrics.stringWidth(text)) / 2;
        int y = (height - metrics.getHeight()) / 2 + metrics.getAscent();
        g2d.drawString(text, x, y);
        
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
