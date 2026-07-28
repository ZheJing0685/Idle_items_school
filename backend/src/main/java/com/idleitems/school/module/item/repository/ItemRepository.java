package com.idleitems.school.module.item.repository;

import com.idleitems.school.module.item.entity.Item;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findByStatus(Item.ItemStatus status, Pageable pageable);
    long countByStatus(Item.ItemStatus status);
    Page<Item> findByUserIdAndStatus(Long userId, Item.ItemStatus status, Pageable pageable);
    long countByUserIdAndStatus(Long userId, Item.ItemStatus status);
    @EntityGraph(attributePaths = {"images"})
    Page<Item> findByUserId(Long userId, Pageable pageable);
    @EntityGraph(attributePaths = {"images"})
    Page<Item> findByCategoryIdAndStatus(Long categoryId, Item.ItemStatus status, Pageable pageable);
    Page<Item> findByCategoryIdInAndStatus(List<Long> categoryIds, Item.ItemStatus status, Pageable pageable);
    
    // 带条件筛选的查询方法（支持关键字搜索）
    @Query("SELECT i FROM Item i WHERE i.status = :status " +
           "AND (:categoryId IS NULL OR i.categoryId = :categoryId) " +
           "AND (:condition IS NULL OR i.condition = :condition) " +
           "AND (:deliveryMethod IS NULL OR i.deliveryMethod = :deliveryMethod) " +
           "AND (:keyword IS NULL OR i.title LIKE %:keyword% OR i.description LIKE %:keyword% OR i.tags LIKE %:keyword% OR i.brand LIKE %:keyword%)")
    Page<Item> findByFilters(@Param("status") Item.ItemStatus status,
                              @Param("categoryId") Long categoryId,
                              @Param("condition") Item.ItemCondition condition,
                              @Param("deliveryMethod") String deliveryMethod,
                              @Param("keyword") String keyword,
                              Pageable pageable);
    
    @Query("SELECT i FROM Item i WHERE i.status = :status " +
           "AND (:categoryIds IS NULL OR i.categoryId IN :categoryIds) " +
           "AND (:condition IS NULL OR i.condition = :condition) " +
           "AND (:deliveryMethod IS NULL OR i.deliveryMethod = :deliveryMethod) " +
           "AND (:keyword IS NULL OR i.title LIKE %:keyword% OR i.description LIKE %:keyword% OR i.tags LIKE %:keyword% OR i.brand LIKE %:keyword%)")
    Page<Item> findByCategoryIdsAndFilters(@Param("status") Item.ItemStatus status,
                                              @Param("categoryIds") List<Long> categoryIds,
                                              @Param("condition") Item.ItemCondition condition,
                                              @Param("deliveryMethod") String deliveryMethod,
                                              @Param("keyword") String keyword,
                                              Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.status = :status AND (i.title LIKE %:keyword% OR i.description LIKE %:keyword%)")
    Page<Item> searchByKeyword(@Param("keyword") String keyword, @Param("status") Item.ItemStatus status, Pageable pageable);

    List<Item> findTop10ByStatusOrderByViewCountDesc(Item.ItemStatus status);

    @Query("SELECT COUNT(i) FROM Item i WHERE i.status = 'ON_SALE' AND i.categoryId = :categoryId")
    Long countByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(i) FROM Item i WHERE i.status = 'ON_SALE' AND i.categoryId IN :categoryIds")
    Long countByCategoryIds(@Param("categoryIds") List<Long> categoryIds);

    @Query("SELECT i.categoryId, COUNT(i) FROM Item i WHERE i.status = 'ON_SALE' AND i.categoryId IN :categoryIds GROUP BY i.categoryId")
    List<Object[]> countByCategoryIdsGrouped(@Param("categoryIds") List<Long> categoryIds);

    @Query("SELECT i.categoryId, COUNT(i) FROM Item i WHERE i.status = 'ON_SALE' " +
           "GROUP BY i.categoryId ORDER BY COUNT(i) DESC")
    List<Object[]> countItemsByCategory();

    List<Item> findByStatusAndCreatedAtBetween(Item.ItemStatus status, LocalDateTime startDate, LocalDateTime endDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")})
    @Query("SELECT i FROM Item i WHERE i.id = :itemId")
    Optional<Item> findItemByIdWithLock(@Param("itemId") Long itemId);

    Long countByUserId(Long userId);

    @Query("SELECT i.userId, COUNT(i) FROM Item i WHERE i.userId IN :userIds GROUP BY i.userId")
    List<Object[]> countByUserIds(@Param("userIds") List<Long> userIds);
    
    /**
     * 原子性将物品标记为已售出
     */
    @Modifying
    @Query("UPDATE Item i SET i.status = 'SOLD' WHERE i.id = :itemId AND i.status = 'ON_SALE'")
    int markItemAsSold(@Param("itemId") Long itemId);

    @Modifying
    @Query("UPDATE Item i SET i.favoriteCount = i.favoriteCount + 1 WHERE i.id = :itemId")
    void incrementFavoriteCount(@Param("itemId") Long itemId);
    
    /**
     * 原子性减少收藏计数
     *
     * @param itemId 物品ID
     */
    @Modifying
    @Query("UPDATE Item i SET i.favoriteCount = i.favoriteCount - 1 WHERE i.id = :itemId AND i.favoriteCount > 0")
    void decrementFavoriteCount(@Param("itemId") Long itemId);
    
    /**
     * 原子性增加浏览量
     *
     * @param itemId 物品ID
     */
    @Modifying
    @Query("UPDATE Item i SET i.viewCount = i.viewCount + 1 WHERE i.id = :itemId")
    void incrementViewCount(@Param("itemId") Long itemId);

    @Modifying
    @Query("UPDATE Item i SET i.viewCount = i.viewCount + :count WHERE i.id = :itemId")
    void incrementViewCountBy(@Param("itemId") Long itemId, @Param("count") int count);

    @Query("SELECT i FROM Item i WHERE i.status = 'ON_SALE' AND i.categoryId = :categoryId AND i.id != :excludeId AND i.price BETWEEN :minPrice AND :maxPrice")
    List<Item> findRelatedByCategoryAndPriceRange(
            @Param("categoryId") Long categoryId,
            @Param("excludeId") Long excludeItemId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.status = 'ON_SALE' AND i.userId = :userId AND i.id != :excludeId")
    List<Item> findOtherItemsBySeller(
            @Param("userId") Long userId,
            @Param("excludeId") Long excludeItemId,
            Pageable pageable);
}
