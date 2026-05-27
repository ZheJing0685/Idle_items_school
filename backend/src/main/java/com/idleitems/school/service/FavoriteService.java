package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.cache.CacheService;
import com.idleitems.school.dto.FavoriteDTO;
import com.idleitems.school.entity.Favorite;
import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.FavoriteRepository;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;

    private static final int MAX_FAVORITES_PER_USER = 500;

    @Transactional
    public Favorite addFavorite(Long userId, Long itemId) {
        log.info("添加收藏，用户ID: {}, 物品ID: {}", userId, itemId);

        if (favoriteRepository.existsByUserIdAndItemId(userId, itemId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "您已收藏过该物品");
        }

        // 检查收藏上限
        long currentCount = favoriteRepository.countByUserId(userId);
        if (currentCount >= MAX_FAVORITES_PER_USER) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "收藏数量已达上限（" + MAX_FAVORITES_PER_USER + "个），请先取消部分收藏");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));

        // 检查物品状态
        if (item.getStatus() != Item.ItemStatus.ON_SALE) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "该物品已下架或已售出，无法收藏");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setItemId(itemId);

        Favorite savedFavorite = favoriteRepository.save(favorite);

        // 原子性增加收藏计数
        itemRepository.incrementFavoriteCount(itemId);
        cacheService.delete(CacheService.getItemKey(itemId));

        log.info("收藏添加成功，收藏ID: {}, 用户当前收藏数: {}", savedFavorite.getId(), currentCount + 1);

        return savedFavorite;
    }

    @Transactional
    public void removeFavorite(Long userId, Long itemId) {
        log.info("取消收藏，用户ID: {}, 物品ID: {}", userId, itemId);

        int deleted = favoriteRepository.deleteByUserIdAndItemId(userId, itemId);
        if (deleted == 0) {
            log.warn("取消收藏失败，收藏记录不存在: userId={}, itemId={}", userId, itemId);
            throw new BusinessException(ErrorCode.CONFLICT, "您未收藏过该物品");
        }

        // 原子性减少收藏计数（确保不低于0）
        itemRepository.decrementFavoriteCount(itemId);
        cacheService.delete(CacheService.getItemKey(itemId));

        log.info("收藏已取消");
    }

    public Page<FavoriteDTO> getUserFavorites(Long userId, Pageable pageable) {
        Page<Favorite> favorites = favoriteRepository.findByUserId(userId, pageable);
        
        if (favorites.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        
        // 批量查询物品信息
        List<Long> itemIds = favorites.getContent().stream()
                .map(Favorite::getItemId)
                .collect(Collectors.toList());
        
        Map<Long, Item> itemMap = itemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, item -> item));
        
        // 批量查询卖家信息
        List<Long> sellerIds = itemMap.values().stream()
                .map(Item::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, String> sellerMap = userRepository.findAllById(sellerIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
        
        // 转换为DTO
        List<FavoriteDTO> dtos = favorites.getContent().stream()
                .map(favorite -> {
                    Item item = itemMap.get(favorite.getItemId());
                    return FavoriteDTO.builder()
                            .id(favorite.getId())
                            .userId(favorite.getUserId())
                            .itemId(favorite.getItemId())
                            .createdAt(favorite.getCreatedAt())
                            .title(item != null ? item.getTitle() : null)
                            .price(item != null ? item.getPrice() : null)
                            .coverImage(item != null ? item.getCoverImage() : null)
                            .status(item != null ? item.getStatus().name() : null)
                            .sellerName(item != null ? sellerMap.get(item.getUserId()) : null)
                            .sellerId(item != null ? item.getUserId() : null)
                            .build();
                })
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, favorites.getTotalElements());
    }

    public boolean isFavorited(Long userId, Long itemId) {
        return favoriteRepository.existsByUserIdAndItemId(userId, itemId);
    }

    /**
     * 批量检查收藏状态（用于物品列表页）
     * 返回 Map<itemId, isFavorited>
     */
    public Map<Long, Boolean> batchCheckFavorited(Long userId, List<Long> itemIds) {
        if (userId == null || itemIds == null || itemIds.isEmpty()) {
            return Map.of();
        }

        List<Favorite> favorites = favoriteRepository.findByUserIdAndItemIdIn(userId, itemIds);
        Set<Long> favoritedItemIds = favorites.stream()
                .map(Favorite::getItemId)
                .collect(Collectors.toSet());

        Map<Long, Boolean> result = new HashMap<>();
        for (Long itemId : itemIds) {
            result.put(itemId, favoritedItemIds.contains(itemId));
        }
        return result;
    }

    public long getFavoriteCount(Long userId) {
        return favoriteRepository.countByUserId(userId);
    }

    /**
     * 同步物品收藏计数
     * 对比favorites表实际记录数与items.favorite_count，修复不一致的数据
     */
    @Transactional
    public int syncFavoriteCount(Long itemId) {
        long actualCount = favoriteRepository.countByItemId(itemId);
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) return 0;

        int currentCount = item.getFavoriteCount() != null ? item.getFavoriteCount() : 0;
        if (currentCount != (int) actualCount) {
            item.setFavoriteCount((int) actualCount);
            itemRepository.save(item);
            cacheService.delete(CacheService.getItemKey(itemId));
            log.info("修复物品收藏计数: itemId={}, 期望={}, 实际={}", itemId, currentCount, actualCount);
            return 1;
        }
        return 0;
    }

    /**
     * 批量同步收藏计数
     */
    @Transactional
    public int batchSyncFavoriteCount(List<Long> itemIds) {
        int fixedCount = 0;
        for (Long itemId : itemIds) {
            fixedCount += syncFavoriteCount(itemId);
        }
        return fixedCount;
    }
}
