package com.idleitems.school.module.category.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.module.category.entity.CategoryFeedback;
import com.idleitems.school.module.category.repository.CategoryFeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryFeedbackService {

    private final CategoryFeedbackRepository categoryFeedbackRepository;

    @Transactional
    public void submitFeedback(Long userId, String feedbackType, Long categoryId, String description) {
        CategoryFeedback feedback = new CategoryFeedback();
        feedback.setUserId(userId);
        feedback.setFeedbackType(CategoryFeedback.FeedbackType.valueOf(feedbackType));
        feedback.setCategoryId(categoryId);
        feedback.setDescription(description);
        feedback.setStatus(CategoryFeedback.FeedbackStatus.PENDING);
        categoryFeedbackRepository.save(feedback);
    }

    public Page<CategoryFeedback> getMyFeedbacks(Long userId, Pageable pageable) {
        return categoryFeedbackRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Page<CategoryFeedback> getAllFeedbacks(String status, Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            return categoryFeedbackRepository.findByStatus(
                    CategoryFeedback.FeedbackStatus.valueOf(status), pageable);
        }
        return categoryFeedbackRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional
    public CategoryFeedback reviewFeedback(Long feedbackId, String action, String reply, Long adminId) {
        CategoryFeedback feedback = categoryFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Feedback not found"));

        try {
            feedback.setStatus(CategoryFeedback.FeedbackStatus.valueOf(action));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid action: " + action);
        }
        feedback.setAdminReply(reply);
        feedback.setReviewedBy(adminId);
        feedback.setReviewedAt(LocalDateTime.now());
        return categoryFeedbackRepository.save(feedback);
    }
}
