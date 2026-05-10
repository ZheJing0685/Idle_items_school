package com.idleitems.school.repository;

import com.idleitems.school.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByChatIdOrderByCreatedAtAsc(Long chatId, Pageable pageable);

    List<ChatMessage> findByChatIdOrderByCreatedAtDesc(Long chatId, Pageable pageable);

    List<ChatMessage> findByChatIdAndReceiverIdAndIsReadFalse(Long chatId, Long receiverId);
}
