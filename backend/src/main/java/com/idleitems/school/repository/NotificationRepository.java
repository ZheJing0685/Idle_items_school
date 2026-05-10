package com.idleitems.school.repository;

import com.idleitems.school.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 获取用户通知列表（分页）
     */
    Page<Notification> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 获取用户未读通知数量
     */
    long countByUserIdAndIsReadFalseAndIsDeletedFalse(Long userId);

    /**
     * 获取用户未读通知列表
     */
    List<Notification> findByUserIdAndIsReadFalseAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);

    /**
     * 标记单条通知为已读
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readTime = :readTime WHERE n.id = :id AND n.userId = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId, @Param("readTime") LocalDateTime readTime);

    /**
     * 标记用户所有通知为已读
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readTime = :readTime WHERE n.userId = :userId AND n.isRead = false AND n.isDeleted = false")
    int markAllAsRead(@Param("userId") Long userId, @Param("readTime") LocalDateTime readTime);

    /**
     * 删除通知（逻辑删除）
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isDeleted = true WHERE n.id = :id AND n.userId = :userId")
    int softDelete(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 根据关联类型和ID查找通知
     */
    List<Notification> findByRelatedIdAndRelatedTypeAndIsDeletedFalse(Long relatedId, String relatedType);

    /**
     * 批量创建通知
     */
    @Modifying
    @Query(value = "INSERT INTO notifications (user_id, notification_type, title, content, related_id, related_type, is_read, is_deleted, created_at) " +
            "VALUES (:userId, :type, :title, :content, :relatedId, :relatedType, false, false, :createdAt)", nativeQuery = true)
    int batchInsert(@Param("userId") Long userId,
                    @Param("type") Integer type,
                    @Param("title") String title,
                    @Param("content") String content,
                    @Param("relatedId") Long relatedId,
                    @Param("relatedType") String relatedType,
                    @Param("createdAt") LocalDateTime createdAt);
}
