package com.idleitems.school.module.chat.repository;

import com.idleitems.school.module.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByChatIdOrderByCreatedAtAsc(Long chatId, Pageable pageable);

    List<ChatMessage> findByChatIdOrderByCreatedAtDesc(Long chatId, Pageable pageable);

    List<ChatMessage> findByChatIdAndReceiverIdAndIsReadFalse(Long chatId, Long receiverId);

    @Query("SELECT m FROM ChatMessage m WHERE m.chatId IN :chatIds AND m.createdAt = " +
           "(SELECT MAX(m2.createdAt) FROM ChatMessage m2 WHERE m2.chatId = m.chatId)")
    List<ChatMessage> findLastMessagesByChatIds(@Param("chatIds") Collection<Long> chatIds);
}
