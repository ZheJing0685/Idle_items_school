package com.idleitems.school.repository;

import com.idleitems.school.entity.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    Page<Chat> findByBuyerIdOrSellerId(Long buyerId, Long sellerId, Pageable pageable);

    Page<Chat> findByBuyerId(Long buyerId, Pageable pageable);

    Page<Chat> findBySellerId(Long sellerId, Pageable pageable);
}
