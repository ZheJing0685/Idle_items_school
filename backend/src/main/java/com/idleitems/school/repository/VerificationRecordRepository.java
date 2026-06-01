package com.idleitems.school.repository;

import com.idleitems.school.entity.VerificationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationRecordRepository extends JpaRepository<VerificationRecord, Long> {
    Optional<VerificationRecord> findByUserId(Long userId);

    List<VerificationRecord> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<VerificationRecord> findByStatus(VerificationRecord.Status status, Pageable pageable);

    Page<VerificationRecord> findByUserIdAndStatus(Long userId, VerificationRecord.Status status, Pageable pageable);
    
    long countByStatus(VerificationRecord.Status status);
    
    long countByCreatedAtAfter(java.time.LocalDateTime createdAt);
}
