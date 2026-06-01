package com.idleitems.school.module.user.repository;

import com.idleitems.school.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    
    org.springframework.data.domain.Page<User> findByRoleAndStatus(User.Role role, User.UserStatus status, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<User> findByRole(User.Role role, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<User> findByStatus(User.UserStatus status, org.springframework.data.domain.Pageable pageable);
    
    long countByStatus(User.UserStatus status);
    long countByVerified(boolean verified);
    long countByCreatedAtAfter(java.time.LocalDateTime createdAt);
}