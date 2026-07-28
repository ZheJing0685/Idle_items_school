package com.idleitems.school.shared.task;

import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class FavoriteCountSyncTask {

    private final ItemRepository itemRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String FAVORITE_COUNT_KEY_PREFIX = "item:favorite:";

    /**
     * 每天凌晨3点同步收藏计数
     * 确保items表中的favorite_count与favorites表实际数据一致
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @SchedulerLock(name = "favoriteCountSync", lockAtLeastFor = "PT5S", lockAtMostFor = "PT30S")
    @Transactional
    public void syncFavoriteCounts() {
        log.info("开始同步收藏计数任务");
        try {
            int batchSize = 1000;
            int pageNumber = 0;
            int updatedCount = 0;
            
            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, batchSize);
                List<Item> items = itemRepository.findByStatus(Item.ItemStatus.ON_SALE, pageable).getContent();
                
                if (items.isEmpty()) {
                    break;
                }
                
                for (Item item : items) {
                    String cacheKey = FAVORITE_COUNT_KEY_PREFIX + item.getId();
                    Object cachedCount = redisTemplate.opsForValue().get(cacheKey);
                    
                    if (cachedCount != null) {
                        int actualCount = ((Number) cachedCount).intValue();
                        if (actualCount != item.getFavoriteCount()) {
                            item.setFavoriteCount(actualCount);
                            itemRepository.save(item);
                            updatedCount++;
                        }
                    }
                }
                
                if (items.size() < batchSize) {
                    break;
                }
                pageNumber++;
            }
            
            if (updatedCount > 0) {
                log.info("收藏计数同步任务完成，更新 {} 个物品的收藏计数", updatedCount);
            } else {
                log.info("收藏计数同步任务完成，无需更新");
            }
        } catch (Exception e) {
            log.error("收藏计数同步任务执行失败: {}", e.getMessage(), e);
        }
    }
}
