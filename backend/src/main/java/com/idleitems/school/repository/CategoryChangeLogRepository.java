package com.idleitems.school.repository;

import com.idleitems.school.entity.CategoryChangeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryChangeLogRepository extends JpaRepository<CategoryChangeLog, Long> {
    Page<CategoryChangeLog> findByCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);
    Page<CategoryChangeLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
