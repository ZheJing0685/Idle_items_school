package com.idleitems.school.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "verification_records")
public class VerificationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;

    @Column(name = "student_id", length = 50)
    private String studentId;

    @Column(name = "id_card", length = 18)
    private String idCard;

    @Column(name = "teacher_id", length = 50)
    private String teacherId;

    @Column(name = "school", length = 100)
    private String school;

    @Column(name = "student_card", length = 255)
    private String studentCard;

    @Column(name = "id_card_front", length = 255)
    private String idCardFront;

    @Column(name = "id_card_back", length = 255)
    private String idCardBack;

    @Column(name = "teacher_card", length = 255)
    private String teacherCard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    public enum Type {
        ID_CARD, STUDENT_CARD, TEACHER_CARD
    }
}