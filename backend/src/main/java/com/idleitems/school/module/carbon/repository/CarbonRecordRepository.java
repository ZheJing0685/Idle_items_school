package com.idleitems.school.module.carbon.repository;

import com.idleitems.school.module.carbon.entity.CarbonRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface CarbonRecordRepository extends JpaRepository<CarbonRecord, Long> {

    /**
     * 统计指定时间段内的总减碳量
     */
    @Query("SELECT COALESCE(SUM(c.carbonSavingKg), 0) FROM CarbonRecord c WHERE c.createdAt BETWEEN :start AND :end")
    BigDecimal findTotalByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 统计指定时间段内的总减碳量（累计，不限时间）
     */
    @Query("SELECT COALESCE(SUM(c.carbonSavingKg), 0) FROM CarbonRecord c")
    BigDecimal findTotalAllTime();

    /**
     * 统计指定时间段内的交易数量
     */
    @Query("SELECT COUNT(c) FROM CarbonRecord c WHERE c.createdAt BETWEEN :start AND :end")
    Long countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 统计指定时间段内的参与人数（去重买家）
     */
    @Query("SELECT COUNT(DISTINCT c.buyerId) FROM CarbonRecord c WHERE c.createdAt BETWEEN :start AND :end")
    Long countDistinctBuyerIdByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
