package com.idleitems.school.module.item.repository;

import com.idleitems.school.module.item.entity.ItemTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemTagRepository extends JpaRepository<ItemTag, Long> {
    List<ItemTag> findByItemId(Long itemId);
}
