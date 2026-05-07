package com.idleitems.school.repository;

import com.idleitems.school.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Page<Category> findBySortGreaterThan(Integer sort, Pageable pageable);
    Page<Category> findBySortLessThan(Integer sort, Pageable pageable);
    Page<Category> findByStatus(Boolean status, Pageable pageable);
    List<Category> findByParentId(Long parentId);
    List<Category> findByParentIdIsNull();
}
