package com.idleitems.school.module.item.service;

import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.order.repository.OrderRepository;
import com.idleitems.school.shared.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemAdminService {

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final CacheService cacheService;

    @Transactional
    public Item approveItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        item.setStatus(Item.ItemStatus.ON_SALE);
        Item savedItem = itemRepository.save(item);
        clearItemCache(id);
        return savedItem;
    }

    @Transactional
    public Item rejectItem(Long id, String reason) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        item.setStatus(Item.ItemStatus.REJECTED);
        item.setRejectReason(reason);
        Item savedItem = itemRepository.save(item);
        clearItemCache(id);
        return savedItem;
    }

    @Transactional
    public Item forceOffShelfItem(Long id, String reason) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        item.setStatus(Item.ItemStatus.OFF_SHELF);
        if (reason != null) {
            item.setRejectReason(reason);
        }
        Item savedItem = itemRepository.save(item);
        clearItemCache(id);
        return savedItem;
    }

    public boolean existsOrderByItemId(Long itemId) {
        return orderRepository.existsByItemId(itemId);
    }

    @Transactional
    public void deleteItemById(Long id) {
        itemRepository.deleteById(id);
        clearItemCache(id);
    }

    public List<Item> getItemsForExport(String keyword, Item.ItemStatus status, Long categoryId) {
        Pageable pageable = Pageable.unpaged();
        if (status != null) {
            return itemRepository.findByStatus(status, pageable).getContent();
        }
        return itemRepository.findAll(pageable).getContent();
    }

    public long countItems() {
        return itemRepository.count();
    }

    public long countItemsByStatus(Item.ItemStatus status) {
        return itemRepository.countByStatus(status);
    }

    public Page<Item> getAdminItems(Pageable pageable, Item.ItemStatus status) {
        if (status != null) {
            return itemRepository.findByStatus(status, pageable);
        }
        return itemRepository.findAll(pageable);
    }

    private void clearItemCache(Long itemId) {
        cacheService.delete(CacheService.getItemKey(itemId));
        cacheService.deletePattern("item:list:*");
        cacheService.deletePattern("item:hot");
    }
}
