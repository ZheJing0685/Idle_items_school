package com.idleitems.school.module.order.repository;

import com.idleitems.school.module.order.entity.Order;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNo(String orderNo);

    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);

    Page<Order> findBySellerId(Long sellerId, Pageable pageable);

    Page<Order> findByOrderStatus(Order.OrderStatus status, Pageable pageable);

    Page<Order> findByBuyerIdAndOrderStatus(Long buyerId, Order.OrderStatus status, Pageable pageable);

    Page<Order> findBySellerIdAndOrderStatus(Long sellerId, Order.OrderStatus status, Pageable pageable);

    @Query("""
            SELECT o FROM Order o
            WHERE (:status IS NULL OR o.orderStatus = :status)
              AND (:paymentMethod IS NULL OR :paymentMethod = '' OR o.paymentMethod = :paymentMethod)
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(o.orderNo) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(o.itemTitle, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(o.buyerName, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Order> searchAdminOrders(
            @Param("keyword") String keyword,
            @Param("status") Order.OrderStatus status,
            @Param("paymentMethod") String paymentMethod,
            Pageable pageable
    );

    @Query("SELECT COUNT(o) FROM Order o WHERE o.sellerId = :sellerId AND o.orderStatus = :status")
    Long countBySellerIdAndStatus(@Param("sellerId") Long sellerId, @Param("status") Order.OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.buyerId = :buyerId AND o.orderStatus = :status")
    Long countByBuyerIdAndStatus(@Param("buyerId") Long buyerId, @Param("status") Order.OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :status")
    Long countByOrderStatus(@Param("status") Order.OrderStatus status);

    @Query("SELECT o.orderStatus, COUNT(o) FROM Order o GROUP BY o.orderStatus")
    List<Object[]> countByOrderStatusGrouped();

    @Query("SELECT COALESCE(SUM(o.price), 0) FROM Order o WHERE o.orderStatus = 'COMPLETED'")
    BigDecimal sumCompletedOrderAmount();

    @Query("SELECT COALESCE(SUM(o.price), 0) FROM Order o WHERE o.orderStatus = 'COMPLETED' AND o.createdAt >= :startDate AND o.createdAt < :endDate")
    BigDecimal sumCompletedOrderAmountByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = 'COMPLETED' AND o.createdAt >= :startDate AND o.createdAt < :endDate")
    Long countCompletedOrdersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<Order> findByOrderStatusAndCreatedAtBetween(Order.OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);

    boolean existsByBuyerIdAndItemIdAndOrderStatusIn(Long buyerId, Long itemId, java.util.List<Order.OrderStatus> statuses);

    boolean existsByItemIdAndOrderStatusInAndIdNot(Long itemId, java.util.List<Order.OrderStatus> statuses, Long excludeOrderId);

    boolean existsByItemId(Long itemId);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")})
    @Query("SELECT o FROM Order o WHERE o.id = :orderId")
    Optional<Order> findByIdWithLock(@Param("orderId") Long orderId);

    @Query("SELECT o FROM Order o WHERE o.orderStatus = :status AND o.createdAt < :threshold")
    List<Order> findTimeoutOrders(@Param("status") Order.OrderStatus status, @Param("threshold") LocalDateTime threshold);

    @Query("SELECT o FROM Order o WHERE o.orderStatus = :status ORDER BY o.createdAt DESC")
    Page<Order> findByOrderStatusOrderByCreatedAtDesc(@Param("status") Order.OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.buyerId = :buyerId OR o.sellerId = :sellerId ORDER BY o.createdAt DESC")
    Page<Order> findByBuyerIdOrSellerIdOrderByCreatedAtDesc(@Param("buyerId") Long buyerId, @Param("sellerId") Long sellerId, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate AND o.orderStatus = :status")
    Long countByCreatedAtAfterAndOrderStatus(@Param("startDate") LocalDateTime startDate, @Param("status") Order.OrderStatus status);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.itemId = :itemId AND o.orderStatus IN :statuses ORDER BY o.createdAt DESC")
    List<Order> findByItemIdAndOrderStatusIn(@Param("itemId") Long itemId, @Param("statuses") List<Order.OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.itemId = :itemId AND o.sellerId = :sellerId ORDER BY o.createdAt DESC")
    List<Order> findByItemIdAndSellerId(@Param("itemId") Long itemId, @Param("sellerId") Long sellerId);

    @Query("""
            SELECT
                FUNCTION('DATE', o.createdAt) as orderDate,
                COUNT(o) as orderCount,
                COALESCE(SUM(CASE WHEN o.orderStatus = 'COMPLETED' THEN o.price ELSE 0 END), 0) as totalAmount
            FROM Order o
            WHERE o.createdAt >= :startDate AND o.createdAt < :endDate
            GROUP BY FUNCTION('DATE', o.createdAt)
            ORDER BY FUNCTION('DATE', o.createdAt)
            """)
    List<Object[]> countOrdersAndAmountGroupedByDate(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 查找发货时间早于指定时间的已发货订单（用于自动确认收货）
     */
    List<Order> findByOrderStatusAndShipTimeBefore(Order.OrderStatus status, LocalDateTime threshold);
}
