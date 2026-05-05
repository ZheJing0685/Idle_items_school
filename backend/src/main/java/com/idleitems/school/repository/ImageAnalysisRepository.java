package com.idleitems.school.repository;

import com.idleitems.school.entity.ImageAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageAnalysisRepository extends JpaRepository<ImageAnalysis, Long> {
    ImageAnalysis findByImageUrl(String imageUrl);
    ImageAnalysis findByItemId(Long itemId);
}
