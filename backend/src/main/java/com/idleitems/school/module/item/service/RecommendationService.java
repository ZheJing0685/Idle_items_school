package com.idleitems.school.module.item.service;

import com.idleitems.school.module.item.dto.ItemSummaryDTO;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.user.repository.UserRepository;
import com.idleitems.school.shared.cache.CacheService;
import com.idleitems.school.util.ItemDTOConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final String PREFIX_BROWSE_CATS = "recommend:browse:cats:";
    private static final String PREFIX_BROWSED_ITEMS = "recommend:browsed:items:";
    private static final String PREFIX_RESULT = "recommend:result:";
    private static final long BROWSE_TTL_DAYS = 30;
    private static final long RESULT_TTL_SECONDS = 300;
    private static final int MAX_CATEGORIES = 3;
    private static final int MAX_RESULT = 8;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemDTOConverter dtoConverter;
    private final CacheService cacheService;

    public void recordView(Long userId, Long itemId, Long categoryId) {
        if (userId == null) return;

        String catsKey = PREFIX_BROWSE_CATS + userId;
        String itemsKey = PREFIX_BROWSED_ITEMS + userId;

        double now = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(catsKey, categoryId, now);
        redisTemplate.opsForSet().add(itemsKey, itemId);

        redisTemplate.expire(catsKey, BROWSE_TTL_DAYS, TimeUnit.DAYS);
        redisTemplate.expire(itemsKey, BROWSE_TTL_DAYS, TimeUnit.DAYS);

        cacheService.delete(PREFIX_RESULT + userId);
    }

    @SuppressWarnings("unchecked")
    public List<ItemSummaryDTO> getRecommendedItems(Long userId) {
        if (userId == null) return getFallbackItems();

        String cacheKey = PREFIX_RESULT + userId;
        Object cached = cacheService.get(cacheKey);
        if (cached instanceof List) {
            List<ItemSummaryDTO> result = (List<ItemSummaryDTO>) cached;
            if (!result.isEmpty()) return result;
        }

        String catsKey = PREFIX_BROWSE_CATS + userId;
        String itemsKey = PREFIX_BROWSED_ITEMS + userId;

        Set<ZSetOperations.TypedTuple<Object>> topCategories =
                redisTemplate.opsForZSet().reverseRangeWithScores(catsKey, 0, MAX_CATEGORIES - 1);

        if (topCategories == null || topCategories.isEmpty()) {
            List<ItemSummaryDTO> fallback = getFallbackItems();
            cacheService.set(cacheKey, fallback, (int) RESULT_TTL_SECONDS, TimeUnit.SECONDS);
            return fallback;
        }

        List<Long> categoryIds = topCategories.stream()
                .map(t -> ((Number) t.getValue()).longValue())
                .toList();

        Set<Object> browsedItemIds = redisTemplate.opsForSet().members(itemsKey);
        Set<Long> excludeIds = browsedItemIds != null
                ? browsedItemIds.stream().map(o -> ((Number) o).longValue()).collect(Collectors.toSet())
                : Collections.emptySet();

        List<Item> candidateItems = itemRepository.findByCategoryIdInAndStatus(
                        categoryIds, Item.ItemStatus.ON_SALE,
                        org.springframework.data.domain.PageRequest.of(0, 50))
                .getContent();

        double now = System.currentTimeMillis();

        List<Item> sorted = candidateItems.stream()
                .filter(i -> !excludeIds.contains(i.getId()))
                .sorted((a, b) -> Double.compare(
                        calculateSimpleHotScore(b, now),
                        calculateSimpleHotScore(a, now)))
                .limit(MAX_RESULT)
                .toList();

        List<ItemSummaryDTO> result = convertToSummaryDTOList(sorted);
        cacheService.set(cacheKey, result, (int) RESULT_TTL_SECONDS, TimeUnit.SECONDS);
        return result;
    }

    private List<ItemSummaryDTO> getFallbackItems() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Item> recentItems = itemRepository.findByStatusAndCreatedAtBetween(
                Item.ItemStatus.ON_SALE, thirtyDaysAgo, LocalDateTime.now());
        double now = System.currentTimeMillis();
        return convertToSummaryDTOList(
                recentItems.stream()
                        .sorted((a, b) -> Double.compare(
                                calculateSimpleHotScore(b, now),
                                calculateSimpleHotScore(a, now)))
                        .limit(MAX_RESULT)
                        .toList());
    }

    private double calculateSimpleHotScore(Item item, double nowMs) {
        long createdMillis = item.getCreatedAt() != null
                ? item.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                : 0L;
        double daysSinceCreation = (nowMs - createdMillis) / (1000.0 * 60 * 60 * 24);
        double timeDecay = 1.0 / (1.0 + daysSinceCreation / 14.0);
        int viewCount = item.getViewCount() != null ? item.getViewCount() : 0;
        int favoriteCount = item.getFavoriteCount() != null ? item.getFavoriteCount() : 0;
        return (viewCount * 0.3 + favoriteCount * 5.0) * timeDecay;
    }

    private List<ItemSummaryDTO> convertToSummaryDTOList(List<Item> items) {
        if (items.isEmpty()) return Collections.emptyList();
        Set<Long> userIds = items.stream().map(Item::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Integer> sellerItemCounts = userIds.isEmpty() ? Map.of() :
                itemRepository.countByUserIds(List.copyOf(userIds)).stream()
                        .collect(Collectors.toMap(row -> (Long) row[0], row -> ((Number) row[1]).intValue()));
        return dtoConverter.toSummaryDTOList(items, userMap, sellerItemCounts);
    }
}
