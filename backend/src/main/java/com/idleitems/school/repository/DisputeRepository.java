package com.idleitems.school.repository;

import com.idleitems.school.entity.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    List<Dispute> findByOrderId(Long orderId);
    List<Dispute> findByDisputeStatus(Dispute.DisputeStatus status);
}
