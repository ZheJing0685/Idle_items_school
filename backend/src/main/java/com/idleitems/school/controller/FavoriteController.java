package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.dto.FavoriteDTO;
import com.idleitems.school.entity.Favorite;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@Tag(name = "收藏管理", description = "用户收藏物品相关接口")
@RestController
@RequestMapping(ApiPaths.Favorite.BASE)
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "添加收藏", description = "收藏指定闲置物品")
    @PostMapping("/{itemId}")
    public Result<Favorite> addFavorite(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long itemId) {
        Favorite favorite = favoriteService.addFavorite(userId, itemId);
        return Result.success("收藏成功", favorite);
    }

    @Operation(summary = "取消收藏", description = "取消收藏指定闲置物品")
    @DeleteMapping("/{itemId}")
    public Result<Void> removeFavorite(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long itemId) {
        favoriteService.removeFavorite(userId, itemId);
        return Result.success("已取消收藏", null);
    }

    @Operation(summary = "获取收藏列表", description = "分页获取当前用户收藏的物品列表")
    @GetMapping
    public Result<Page<FavoriteDTO>> getUserFavorites(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FavoriteDTO> favorites = favoriteService.getUserFavorites(userId, pageable);
        return Result.success(favorites);
    }

    @Operation(summary = "检查收藏状态", description = "检查当前用户是否已收藏指定物品")
    @GetMapping("/{itemId}/status")
    public Result<Boolean> checkFavorite(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long itemId) {
        boolean isFavorited = favoriteService.isFavorited(userId, itemId);
        return Result.success(isFavorited);
    }
}
