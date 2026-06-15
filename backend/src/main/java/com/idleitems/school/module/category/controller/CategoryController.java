package com.idleitems.school.module.category.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.module.category.dto.SubmitFeedbackRequest;
import com.idleitems.school.module.category.entity.CategoryFeedback;
import com.idleitems.school.module.category.service.CategoryQueryService;
import com.idleitems.school.module.category.service.CategoryCommandService;
import com.idleitems.school.module.category.service.CategoryFeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;

@Tag(name = "分类管理", description = "物品分类查询相关接口")
@RestController
@RequestMapping(ApiPaths.Category.BASE)
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryQueryService categoryQueryService;
    private final CategoryFeedbackService categoryFeedbackService;

    @Operation(summary = "获取分类列表", description = "获取所有物品分类列表")
    @GetMapping(ApiPaths.Category.LIST_PATH)
    public Result<List<Map<String, Object>>> getCategories() {
        return Result.success(categoryQueryService.getAllCategories());
    }

    @Operation(summary = "获取分类树", description = "获取物品分类的树形结构数据")
    @GetMapping(ApiPaths.Category.TREE_PATH)
    public Result<List<Map<String, Object>>> getCategoryTree() {
        return Result.success(categoryQueryService.getCategoryTree());
    }

    @Operation(summary = "获取子分类列表", description = "根据父分类ID获取直接子分类列表")
    @GetMapping("/{parentId}/children")
    public Result<List<Map<String, Object>>> getChildren(@PathVariable Long parentId) {
        return Result.success(categoryQueryService.getChildren(parentId));
    }

    @Operation(summary = "搜索分类", description = "根据关键字搜索物品分类")
    @GetMapping("/search")
    public Result<List<Map<String, Object>>> searchCategories(@RequestParam String keyword) {
        return Result.success(categoryQueryService.searchCategories(keyword));
    }

    @Operation(summary = "分类搜索建议", description = "根据前缀关键词快速搜索分类（仅返回前5条活跃分类）")
    @GetMapping(ApiPaths.Category.SUGGEST_PATH)
    public Result<List<Map<String, Object>>> suggestCategories(@RequestParam("q") String prefix) {
        return Result.success(categoryQueryService.suggestCategories(prefix));
    }

    @Operation(summary = "智能分类推荐", description = "根据物品标题推荐最匹配的分类（用于发布物品时智能选择）")
    @GetMapping("/recommend")
    public Result<List<Map<String, Object>>> recommendCategories(
            @RequestParam String title,
            @RequestParam(defaultValue = "3") int limit) {
        return Result.success(categoryQueryService.recommendCategories(title, limit));
    }

    @Operation(summary = "分类热度排行", description = "获取物品数量最多的热门分类排行")
    @GetMapping("/hot")
    public Result<List<Map<String, Object>>> getHotCategories(
            @RequestParam(defaultValue = "8") int limit) {
        return Result.success(categoryQueryService.getHotCategories(limit));
    }

    @Operation(summary = "分类面包屑", description = "获取指定分类的父级路径（从根到当前分类）")
    @GetMapping(ApiPaths.Category.BREADCRUMB_PATH)
    public Result<List<Map<String, Object>>> getBreadcrumb(@PathVariable Long id) {
        return Result.success(categoryQueryService.getBreadcrumb(id));
    }

    @Operation(summary = "提交分类反馈", description = "用户提交分类相关的反馈建议")
    @PostMapping("/feedback")
    public Result<Void> submitFeedback(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody SubmitFeedbackRequest request) {
        categoryFeedbackService.submitFeedback(userId, request.getFeedbackType(), request.getCategoryId(), request.getDescription());
        return Result.success("反馈提交成功", null);
    }

    @Operation(summary = "获取我的反馈", description = "获取当前用户提交的分类反馈列表")
    @GetMapping("/feedback/my")
    public Result<Page<CategoryFeedback>> getMyFeedbacks(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(categoryFeedbackService.getMyFeedbacks(userId, pageable));
    }
}
