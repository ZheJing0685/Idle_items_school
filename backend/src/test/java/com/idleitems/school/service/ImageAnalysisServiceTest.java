package com.idleitems.school.service;

import com.idleitems.school.entity.ImageAnalysis;
import com.idleitems.school.repository.ImageAnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("ImageAnalysisService 单元测试")
class ImageAnalysisServiceTest {

    @Mock
    private ImageAnalysisRepository imageAnalysisRepository;

    @InjectMocks
    private ImageAnalysisService imageAnalysisService;

    private ImageAnalysis testAnalysis;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testAnalysis = new ImageAnalysis();
        testAnalysis.setId(1L);
        testAnalysis.setImageUrl("https://example.com/image.jpg");
        testAnalysis.setItemId(1L);
        testAnalysis.setItemType("电子设备");
        testAnalysis.setBrand("Apple");
        testAnalysis.setColor("银色");
        testAnalysis.setConfidence(BigDecimal.valueOf(85.0));
        testAnalysis.setStatus(ImageAnalysis.Status.SUCCESS);
    }

    @Test
    @DisplayName("测试分析图片 - 成功")
    void testAnalyzeImageSuccess() {
        when(imageAnalysisRepository.save(any(ImageAnalysis.class))).thenReturn(testAnalysis);

        String imageUrl = "https://example.com/test.jpg";
        Long itemId = 1L;

        Map<String, Object> result = imageAnalysisService.analyzeImage(imageUrl, itemId);

        assertNotNull(result);
        assertTrue(result.containsKey("itemType"));
        assertTrue(result.containsKey("brand"));
        assertTrue(result.containsKey("color"));
        assertTrue(result.containsKey("confidence"));
        assertEquals("电子设备", result.get("itemType"));
        assertEquals("Apple", result.get("brand"));
        assertEquals("银色", result.get("color"));
        assertEquals(85.0, result.get("confidence"));

        verify(imageAnalysisRepository, times(1)).save(any(ImageAnalysis.class));
    }

    @Test
    @DisplayName("测试分析结果包含预期字段")
    void testAnalyzeImageResultFields() {
        when(imageAnalysisRepository.save(any(ImageAnalysis.class))).thenReturn(testAnalysis);

        Map<String, Object> result = imageAnalysisService.analyzeImage("https://example.com/test.jpg", 1L);

        assertNotNull(result);
        assertTrue(result.containsKey("features"));
        assertTrue(result.get("features") instanceof String[]);
        
        String[] features = (String[]) result.get("features");
        assertEquals(3, features.length);
    }

    @Test
    @DisplayName("测试保存的分析记录数据正确")
    void testSavedAnalysisData() {
        when(imageAnalysisRepository.save(any(ImageAnalysis.class))).thenAnswer(invocation -> {
            ImageAnalysis saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        String imageUrl = "https://example.com/test.jpg";
        Long itemId = 1L;

        imageAnalysisService.analyzeImage(imageUrl, itemId);

        verify(imageAnalysisRepository, times(1)).save(argThat(analysis -> {
            assertEquals(imageUrl, analysis.getImageUrl());
            assertEquals(itemId, analysis.getItemId());
            assertEquals("电子设备", analysis.getItemType());
            assertEquals("Apple", analysis.getBrand());
            assertEquals("银色", analysis.getColor());
            assertEquals(BigDecimal.valueOf(85.0), analysis.getConfidence());
            assertEquals(ImageAnalysis.Status.SUCCESS, analysis.getStatus());
            return true;
        }));
    }

    @Test
    @DisplayName("测试根据图片URL查找分析结果")
    void testGetAnalysisByImageUrl() {
        when(imageAnalysisRepository.findByImageUrl("https://example.com/image.jpg")).thenReturn(testAnalysis);

        ImageAnalysis result = imageAnalysisService.getAnalysisByImageUrl("https://example.com/image.jpg");

        assertNotNull(result);
        assertEquals("https://example.com/image.jpg", result.getImageUrl());
        verify(imageAnalysisRepository, times(1)).findByImageUrl("https://example.com/image.jpg");
    }

    @Test
    @DisplayName("测试根据物品ID查找分析结果")
    void testGetAnalysisByItemId() {
        when(imageAnalysisRepository.findByItemId(1L)).thenReturn(testAnalysis);

        ImageAnalysis result = imageAnalysisService.getAnalysisByItemId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getItemId());
        verify(imageAnalysisRepository, times(1)).findByItemId(1L);
    }

    @Test
    @DisplayName("测试根据图片URL查找 - 未找到")
    void testGetAnalysisByImageUrlNotFound() {
        when(imageAnalysisRepository.findByImageUrl("https://example.com/nonexistent.jpg")).thenReturn(null);

        ImageAnalysis result = imageAnalysisService.getAnalysisByImageUrl("https://example.com/nonexistent.jpg");

        assertNull(result);
        verify(imageAnalysisRepository, times(1)).findByImageUrl("https://example.com/nonexistent.jpg");
    }

    @Test
    @DisplayName("测试根据物品ID查找 - 未找到")
    void testGetAnalysisByItemIdNotFound() {
        when(imageAnalysisRepository.findByItemId(999L)).thenReturn(null);

        ImageAnalysis result = imageAnalysisService.getAnalysisByItemId(999L);

        assertNull(result);
        verify(imageAnalysisRepository, times(1)).findByItemId(999L);
    }

    @Test
    @DisplayName("测试置信度数据类型正确")
    void testConfidenceDataType() {
        when(imageAnalysisRepository.save(any(ImageAnalysis.class))).thenReturn(testAnalysis);

        Map<String, Object> result = imageAnalysisService.analyzeImage("https://example.com/test.jpg", 1L);

        assertTrue(result.get("confidence") instanceof Double);
        assertEquals(85.0, (Double) result.get("confidence"), 0.01);
    }
}
