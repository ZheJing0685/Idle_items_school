package com.idleitems.school.module.item.service;

import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.shared.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountService {

    private static final String VIEW_COUNT_PREFIX = "view:buffer:";
    private static final long VIEW_BUFFER_TTL_SECONDS = 3600;
    private static final int FLUSH_THRESHOLD = 10;
    private static final String VIEW_FLUSH_QUEUE = "view:flush:queue";

    private final ItemRepository itemRepository;
    private final CacheService cacheService;
    private final RedisTemplate<String, String> redisTemplate;

    @Async("viewCountExecutor")
    public void increment(Long itemId) {
        try {
            String key = VIEW_COUNT_PREFIX + itemId;
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, VIEW_BUFFER_TTL_SECONDS, TimeUnit.SECONDS);
                redisTemplate.opsForSet().add(VIEW_FLUSH_QUEUE, String.valueOf(itemId));
            }
            cacheService.delete(CacheService.getItemKey(itemId));

            if (count != null && count >= FLUSH_THRESHOLD) {
                flushToDb(itemId);
            }
        } catch (Exception e) {
            log.warn("浏览计数缓冲失败，直接写入DB: itemId={}", itemId, e);
            try {
                itemRepository.incrementViewCount(itemId);
                cacheService.delete(CacheService.getItemKey(itemId));
            } catch (Exception ex) {
                log.error("浏览计数写入DB失败: itemId={}", itemId, ex);
            }
        }
    }

    public void flushToDb(Long itemId) {
        try {
            String key = VIEW_COUNT_PREFIX + itemId;
            String val = redisTemplate.opsForValue().get(key);
            if (val == null) return;
            redisTemplate.delete(key);

            long bufferedCount = Long.parseLong(val);
            if (bufferedCount <= 0) return;

            itemRepository.incrementViewCountBy(itemId, (int) bufferedCount);
            redisTemplate.opsForSet().remove(VIEW_FLUSH_QUEUE, String.valueOf(itemId));
            log.debug("浏览计数已刷新: itemId={}, count={}", itemId, bufferedCount);
        } catch (Exception e) {
            log.warn("刷新浏览计数失败: itemId={}", itemId, e);
        }
    }

    @Scheduled(fixedRate = 300_000)
    public void flushAll() {
        Set<String> members = redisTemplate.opsForSet().members(VIEW_FLUSH_QUEUE);
        if (members == null || members.isEmpty()) return;
        log.info("定时刷新浏览计数: {}个物品待处理", members.size());
        for (String itemId : members) {
            flushToDb(Long.parseLong(itemId));
        }
    }
}
