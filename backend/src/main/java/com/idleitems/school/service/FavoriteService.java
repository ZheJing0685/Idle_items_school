package com.idleitems.school.service;

import com.idleitems.school.entity.Favorite;
import com.idleitems.school.entity.Item;
import com.idleitems.school.repository.FavoriteRepository;
import com.idleitems.school.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ItemRepository itemRepository;

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
        log.info("收藏已取消");
    }

    public Page<Favorite> getUserFavorites(Long userId, Pageable pageable) {
        return favoriteRepository.findByUserId(userId, pageable);
    }

    public boolean isFavorited(Long userId, Long itemId) {
        return favoriteRepository.existsByUserIdAndItemId(userId, itemId);
    }

    public long getFavoriteCount(Long userId) {
        return favoriteRepository.countByUserId(userId);
    }
}
