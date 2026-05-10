package com.idleitems.school.service;

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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    public Favorite addFavorite(Long userId, Long itemId) {
        log.info("添加收藏，用户ID: {}, 物品ID: {}", userId, itemId);

        if (favoriteRepository.existsByUserIdAndItemId(userId, itemId)) {
            throw new IllegalArgumentException("您已收藏过该物品");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setItemId(itemId);

        Favorite savedFavorite = favoriteRepository.save(favorite);
        
        // 原子性增加收藏计数
        itemRepository.incrementFavoriteCount(itemId);
        
        log.info("收藏添加成功，收藏ID: {}", savedFavorite.getId());

        return savedFavorite;
    }

    @Transactional
    public void removeFavorite(Long userId, Long itemId) {
        log.info("取消收藏，用户ID: {}, 物品ID: {}", userId, itemId);

        if (!favoriteRepository.existsByUserIdAndItemId(userId, itemId)) {
            throw new IllegalArgumentException("您未收藏过该物品");
        }

        favoriteRepository.deleteByUserIdAndItemId(userId, itemId);
        
        // 原子性减少收藏计数
        itemRepository.decrementFavoriteCount(itemId);
        
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

    public long getFavoriteCount(Long userId) {
        return favoriteRepository.countByUserId(userId);
    }
}
