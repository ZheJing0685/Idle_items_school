package com.idleitems.school.repository;

import com.idleitems.school.entity.Item;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findByStatus(Item.ItemStatus status, Pageable pageable);
    Page<Item> findByUserIdAndStatus(Long userId, Item.ItemStatus status, Pageable pageable);
    Page<Item> findByUserId(Long userId, Pageable pageable);
    Page<Item> findByCategoryIdAndStatus(Long categoryId, Item.ItemStatus status, Pageable pageable);
    Page<Item> findByCategoryIdInAndStatus(List<Long> categoryIds, Item.ItemStatus status, Pageable pageable);
    
    // 带条件筛选的查询方法
    @Query("SELECT i FROM Item i WHERE i.status = :status " +
           "AND (:categoryId IS NULL OR i.categoryId = :categoryId) " +
           "AND (:condition IS NULL OR i.condition = :condition) " +
           "AND (:deliveryMethod IS NULL OR i.deliveryMethod = :deliveryMethod)")
    Page<Item> findByFilters(@Param("status") Item.ItemStatus status,
                              @Param("categoryId") Long categoryId,
                              @Param("condition") String condition,
                              @Param("deliveryMethod") Integer deliveryMethod,
                              Pageable pageable);
    
    @Query("SELECT i FROM Item i WHERE i.status = :status " +
           "AND (:categoryIds IS NULL OR i.categoryId IN :categoryIds) " +
           "AND (:condition IS NULL OR i.condition = :condition) " +
           "AND (:deliveryMethod IS NULL OR i.deliveryMethod = :deliveryMethod)")
    Page<Item> findByCategoryIdsAndFilters(@Param("status") Item.ItemStatus status,
                                            @Param("categoryIds") List<Long> categoryIds,
                                            @Param("condition") String condition,
                                            @Param("deliveryMethod") Integer deliveryMethod,
                                            Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.status = :status AND (i.title LIKE %:keyword% OR i.description LIKE %:keyword%)")
    Page<Item> searchByKeyword(@Param("keyword") String keyword, @Param("status") Item.ItemStatus status, Pageable pageable);

    List<Item> findTop10ByStatusOrderByViewCountDesc(Item.ItemStatus status);

    @Query("SELECT COUNT(i) FROM Item i WHERE i.status = 'ON_SALE' AND i.categoryId = :categoryId")
    Long countByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(i) FROM Item i WHERE i.status = 'ON_SALE' AND i.categoryId IN :categoryIds")
    Long countByCategoryIds(@Param("categoryIds") List<Long> categoryIds);

    List<Item> findByStatusAndCreatedAtBetween(Item.ItemStatus status, LocalDateTime startDate, LocalDateTime endDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")})
    @Query("SELECT i FROM Item i WHERE i.id = :itemId")
    Optional<Item> findItemByIdWithLock(@Param("itemId") Long itemId);

    Long countByUserId(Long userId);

    @Query("SELECT i.userId, COUNT(i) FROM Item i WHERE i.userId IN :userIds GROUP BY i.userId")
    List<Object[]> countByUserIds(@Param("userIds") List<Long> userIds);
}
