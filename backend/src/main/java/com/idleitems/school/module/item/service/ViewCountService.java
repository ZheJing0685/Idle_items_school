package com.idleitems.school.module.item.service;

import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.shared.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountService {

    private final ItemRepository itemRepository;
    private final CacheService cacheService;

    @Async("viewCountExecutor")
    @Transactional
    public void increment(Long itemId) {
        try {
            itemRepository.incrementViewCount(itemId);
            cacheService.delete(CacheService.getItemKey(itemId));
            cacheService.delete(CacheService.getHotItemsKey());
            log.debug("View count incremented for item: {}", itemId);
        } catch (Exception e) {
            log.error("Failed to increment view count for item {}: {}", itemId, e.getMessage());
        }
    }
}
