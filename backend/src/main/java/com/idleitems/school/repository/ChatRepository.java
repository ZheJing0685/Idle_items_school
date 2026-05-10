package com.idleitems.school.repository;

import com.idleitems.school.entity.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    Page<Chat> findByBuyerIdOrSellerId(Long buyerId, Long sellerId, Pageable pageable);
    
    @Query("SELECT c FROM Chat c WHERE c.buyerId = :userId OR c.sellerId = :userId ORDER BY c.updatedAt DESC")
    List<Chat> findAllChatsByUserId(@Param("userId") Long userId);

    Page<Chat> findByBuyerId(Long buyerId, Pageable pageable);

    Page<Chat> findBySellerId(Long sellerId, Pageable pageable);
}
