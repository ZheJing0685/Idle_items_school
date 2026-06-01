package com.idleitems.school.module.category.repository;

import com.idleitems.school.module.category.entity.CategoryFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryFeedbackRepository extends JpaRepository<CategoryFeedback, Long> {
    Page<CategoryFeedback> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<CategoryFeedback> findByStatus(CategoryFeedback.FeedbackStatus status, Pageable pageable);
    Page<CategoryFeedback> findAllByOrderByCreatedAtDesc(Pageable pageable);
    long countByStatus(CategoryFeedback.FeedbackStatus status);
}
