package com.idleitems.school.module.admin.repository;

import com.idleitems.school.module.admin.entity.AdminLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {

    Page<AdminLog> findByAdminId(Long adminId, Pageable pageable);

    Page<AdminLog> findByOperationContainingOrTargetTypeContaining(String operation, String targetType, Pageable pageable);

    @Query("SELECT al FROM AdminLog al WHERE al.adminId = :adminId ORDER BY al.createdAt DESC")
    Page<AdminLog> findByAdminIdOrderByCreatedAtDesc(@Param("adminId") Long adminId, Pageable pageable);

    @Query("SELECT COUNT(al) FROM AdminLog al WHERE al.createdAt >= :startDate")
    Long countByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT al.targetType, COUNT(al) as count FROM AdminLog al GROUP BY al.targetType")
    List<Object[]> countGroupByTargetType();

    @Query("SELECT al.operation, COUNT(al) FROM AdminLog al GROUP BY al.operation ORDER BY COUNT(al) DESC")
    List<Object[]> countGroupByOperation();

    @Query("SELECT al.adminId, COUNT(al) FROM AdminLog al GROUP BY al.adminId ORDER BY COUNT(al) DESC LIMIT 5")
    List<Object[]> findTop5AdminsByOperationCount();

    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT al FROM AdminLog al WHERE " +
           "(:keyword IS NULL OR al.operation LIKE %:keyword% OR al.targetType LIKE %:keyword%) AND " +
           "(:adminId IS NULL OR al.adminId = :adminId) AND " +
           "(:targetType IS NULL OR al.targetType = :targetType) AND " +
           "(:startDate IS NULL OR al.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR al.createdAt <= :endDate) " +
           "ORDER BY al.createdAt DESC")
    Page<AdminLog> findByFilters(
            @Param("keyword") String keyword,
            @Param("adminId") Long adminId,
            @Param("targetType") String targetType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT al FROM AdminLog al WHERE " +
           "(:keyword IS NULL OR al.operation LIKE %:keyword% OR al.targetType LIKE %:keyword%) AND " +
           "(:adminId IS NULL OR al.adminId = :adminId) AND " +
           "(:targetType IS NULL OR al.targetType = :targetType) AND " +
           "(:startDate IS NULL OR al.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR al.createdAt <= :endDate) " +
           "ORDER BY al.createdAt DESC")
    List<AdminLog> findAllByFilters(
            @Param("keyword") String keyword,
            @Param("adminId") Long adminId,
            @Param("targetType") String targetType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
