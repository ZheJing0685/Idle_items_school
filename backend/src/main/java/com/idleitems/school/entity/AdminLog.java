package com.idleitems.school.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "admin_logs")
@EntityListeners(AuditingEntityListener.class)
public class AdminLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "operation")
    private String operation;

    @Column(name = "target_type")
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;
}
