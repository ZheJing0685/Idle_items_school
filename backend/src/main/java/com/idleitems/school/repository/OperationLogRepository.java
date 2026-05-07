package com.idleitems.school.repository;

import com.idleitems.school.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    Page<OperationLog> findByUserId(Long userId, Pageable pageable);

    Page<OperationLog> findByOperationContaining(String operation, Pageable pageable);
}
