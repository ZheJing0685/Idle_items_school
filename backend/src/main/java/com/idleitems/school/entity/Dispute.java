package com.idleitems.school.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "disputes")
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Column(name = "respondent_id", nullable = false)
    private Long respondentId;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "evidence_images", columnDefinition = "TEXT")
    private String evidenceImages;

    @Enumerated(EnumType.STRING)
    @Column(name = "dispute_status", nullable = false)
    private DisputeStatus disputeStatus = DisputeStatus.PENDING;

    @Column(name = "handler_id")
    private Long handlerId;

    @Column(name = "result", length = 500)
    private String result;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum DisputeStatus {
        PENDING, PROCESSING, RESOLVED, CLOSED
    }
}