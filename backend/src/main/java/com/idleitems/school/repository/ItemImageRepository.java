package com.idleitems.school.repository;

import com.idleitems.school.entity.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {
    List<ItemImage> findByItemId(Long itemId);
}
