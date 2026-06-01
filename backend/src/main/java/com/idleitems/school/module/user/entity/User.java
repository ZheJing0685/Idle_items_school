package com.idleitems.school.module.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_role", columnList = "role"),
    @Index(name = "idx_users_status", columnList = "status"),
    @Index(name = "idx_users_is_deleted", columnList = "is_deleted"),
    @Index(name = "idx_users_role_status", columnList = "role, status")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(unique = true, length = 100)
    private String email;

    @Column(unique = true, length = 20)
    private String phone;

    @Column(length = 50)
    private String nickname;

    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.STUDENT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "verified")
    private Boolean verified = false;

    @Column(name = "student_id", length = 50)
    private String studentId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "create_by")
    private Long createBy;

    @Column(name = "update_by")
    private Long updateBy;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;

    @Column(name = "login_count", nullable = false)
    private Integer loginCount = 0;

    @Column(name = "credit_score", nullable = false)
    private Integer creditScore = 100;

    @Column(name = "total_transactions", nullable = false)
    private Integer totalTransactions = 0;

    @Column(name = "total_sales", nullable = false)
    private Integer totalSales = 0;

    @Column(name = "total_purchases", nullable = false)
    private Integer totalPurchases = 0;

    @Column(name = "gender")
    private Integer gender;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "school_name", length = 100)
    private String schoolName;

    public enum Role {
        STUDENT, ADMIN
    }

    public enum UserStatus {
        ACTIVE, DISABLED
    }
}