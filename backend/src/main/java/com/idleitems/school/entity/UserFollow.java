package com.idleitems.school.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_follows", uniqueConstraints = {
    @UniqueConstraint(name = "uk_follow", columnNames = {"follower_id", "following_id"})
})
public class UserFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follower_id", nullable = false)
    private Long followerId;

    @Column(name = "following_id", nullable = false)
    private Long followingId;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
