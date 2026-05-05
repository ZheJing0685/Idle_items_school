package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.entity.Favorite;
import com.idleitems.school.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{itemId}")
    public Result<Favorite> addFavorite(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long itemId) {
        Favorite favorite = favoriteService.addFavorite(userId, itemId);
        return Result.success("收藏成功", favorite);
    }

    @DeleteMapping("/{itemId}")
    public Result<Void> removeFavorite(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long itemId) {
        favoriteService.removeFavorite(userId, itemId);
        return Result.success("已取消收藏", null);
    }

    @GetMapping
    public Result<Page<Favorite>> getUserFavorites(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Favorite> favorites = favoriteService.getUserFavorites(userId, pageable);
        return Result.success(favorites);
    }

    @GetMapping("/{itemId}/status")
    public Result<Boolean> checkFavorite(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long itemId) {
        boolean isFavorited = favoriteService.isFavorited(userId, itemId);
        return Result.success(isFavorited);
    }
}
