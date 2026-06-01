package com.idleitems.school.module.dispute.repository;

import com.idleitems.school.module.dispute.entity.Dispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    List<Dispute> findByOrderId(Long orderId);

    List<Dispute> findByDisputeStatus(Dispute.DisputeStatus status);

    long countByDisputeStatus(Dispute.DisputeStatus status);

    boolean existsByOrderIdAndDisputeStatusIn(Long orderId, Collection<Dispute.DisputeStatus> statuses);

    Page<Dispute> findByApplicantIdAndDisputeStatusOrderByCreatedAtDesc(Long applicantId, Dispute.DisputeStatus status, Pageable pageable);

    Page<Dispute> findByApplicantIdOrRespondentIdOrderByCreatedAtDesc(Long applicantId, Long respondentId, Pageable pageable);

    Page<Dispute> findByDisputeStatusOrderByCreatedAtDesc(Dispute.DisputeStatus status, Pageable pageable);

    List<Dispute> findByHandlerId(Long handlerId);

    List<Dispute> findByIsEscalatedTrue();

    long countByIsEscalatedTrue();

    long countByPriorityAndDisputeStatus(Integer priority, Dispute.DisputeStatus status);

    @Query("SELECT d FROM Dispute d WHERE d.disputeStatus = :status ORDER BY d.priority DESC, d.createdAt ASC")
    List<Dispute> findByStatusOrderByPriority(@Param("status") Dispute.DisputeStatus status);

    @Query("SELECT d FROM Dispute d WHERE d.disputeStatus IN :statuses AND d.assignTime IS NULL ORDER BY d.priority DESC, d.createdAt ASC")
    List<Dispute> findUnassignedByStatuses(@Param("statuses") Collection<Dispute.DisputeStatus> statuses);

    @Query("SELECT d FROM Dispute d WHERE d.disputeStatus = :status AND d.createdAt < :beforeTime")
    List<Dispute> findByStatusCreatedBefore(@Param("status") Dispute.DisputeStatus status, @Param("beforeTime") LocalDateTime beforeTime);

    @Query("SELECT d FROM Dispute d WHERE d.applicantId = :userId OR d.respondentId = :userId")
    Page<Dispute> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT d FROM Dispute d WHERE d.orderId = :orderId AND d.disputeStatus NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')")
    List<Dispute> findActiveByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT COUNT(d) FROM Dispute d WHERE d.disputeStatus IN ('PENDING', 'ASSIGNED') AND d.isUrgent = true")
    long countUrgentPending();

    @Query("SELECT d FROM Dispute d WHERE d.disputeStatus IN ('PENDING', 'ASSIGNED') AND d.createdAt < :threshold")
    List<Dispute> findLongPendingDisputes(@Param("threshold") LocalDateTime threshold);
}