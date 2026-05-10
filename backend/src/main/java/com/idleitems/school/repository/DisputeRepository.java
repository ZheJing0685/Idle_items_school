package com.idleitems.school.repository;

import com.idleitems.school.entity.Dispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
