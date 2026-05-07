package com.idleitems.school.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "operation_logs")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 50)
    private String operation;

    @Column(length = 200)
    private String method;

    @Column(columnDefinition = "TEXT")
    private String params;

    @Column(length = 50)
    private String ip;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
