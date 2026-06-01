package com.idleitems.school.module.order.repository;

import com.idleitems.school.module.order.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByReviewedUserId(Long reviewedUserId, Pageable pageable);

    Page<Review> findByItemId(Long itemId, Pageable pageable);

    Page<Review> findByOrderId(Long orderId, Pageable pageable);

    boolean existsByOrderIdAndReviewerId(Long orderId, Long reviewerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewedUserId = :userId")
    BigDecimal getAverageRatingByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.reviewedUserId = :userId")
    Long countByReviewedUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.itemId = :itemId")
    BigDecimal getAverageRatingByItemId(@Param("itemId") Long itemId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.itemId = :itemId")
    Long countByItemId(@Param("itemId") Long itemId);

    @Query("SELECT r.orderId FROM Review r WHERE r.orderId IN :orderIds AND r.reviewerId = :reviewerId")
    List<Long> findReviewedOrderIds(
            @Param("orderIds") List<Long> orderIds,
            @Param("reviewerId") Long reviewerId
    );

    @Query("SELECT r.reviewedUserId, AVG(r.rating) FROM Review r WHERE r.reviewedUserId IN :userIds GROUP BY r.reviewedUserId")
    List<Object[]> getAverageRatingsByUserIds(@Param("userIds") List<Long> userIds);
}
