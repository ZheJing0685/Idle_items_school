package com.idleitems.school.service;

import com.idleitems.school.entity.ImageAnalysis;
import com.idleitems.school.repository.ImageAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageAnalysisService {

    private final ImageAnalysisRepository imageAnalysisRepository;

    public Map<String, Object> analyzeImage(String imageUrl, Long itemId) {
        Map<String, Object> analysisResult = new HashMap<>();

        try {
            // TODO: 接入实际 AI 图像分析服务
            analysisResult.put("success", true);
            analysisResult.put("labels", Collections.emptyList());
            analysisResult.put("suggestedCategory", null);
            analysisResult.put("confidence", 0.0);
            analysisResult.put("degraded", true);

            ImageAnalysis analysis = new ImageAnalysis();
            analysis.setImageUrl(imageUrl);
            analysis.setItemId(itemId);
            analysis.setAnalysisResult(analysisResult);
            analysis.setStatus(ImageAnalysis.Status.SUCCESS);
            analysis.setConfidence(BigDecimal.ZERO);

            imageAnalysisRepository.save(analysis);
        } catch (Exception e) {
            log.warn("图像分析服务不可用，返回默认结果: {}", e.getMessage());
            analysisResult.put("success", true);
            analysisResult.put("labels", Collections.emptyList());
            analysisResult.put("suggestedCategory", null);
            analysisResult.put("confidence", 0.0);
            analysisResult.put("degraded", true);
        }

        return analysisResult;
    }

    public ImageAnalysis getAnalysisByImageUrl(String imageUrl) {
        return imageAnalysisRepository.findByImageUrl(imageUrl);
    }

    public ImageAnalysis getAnalysisByItemId(Long itemId) {
        return imageAnalysisRepository.findByItemId(itemId);
    }
}