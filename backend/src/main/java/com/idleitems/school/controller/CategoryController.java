package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.dto.SubmitFeedbackRequest;
import com.idleitems.school.entity.CategoryFeedback;
import com.idleitems.school.service.CategoryQueryService;
import com.idleitems.school.service.CategoryCommandService;
import com.idleitems.school.service.CategoryFeedbackService;
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

    @Operation(summary = "搜索分类", description = "根据关键字搜索物品分类")
    @GetMapping("/search")
    public Result<List<Map<String, Object>>> searchCategories(@RequestParam String keyword) {
        return Result.success(categoryQueryService.searchCategories(keyword));
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
