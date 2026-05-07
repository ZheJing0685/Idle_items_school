package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.entity.CategoryFeedback;
import com.idleitems.school.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiPaths.Category.BASE)
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping(ApiPaths.Category.LIST_PATH)
    public Result<List<Map<String, Object>>> getCategories() {
        return Result.success(categoryService.getAllCategories());
    }

    @GetMapping(ApiPaths.Category.TREE_PATH)
    public Result<List<Map<String, Object>>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    @GetMapping("/search")
    public Result<List<Map<String, Object>>> searchCategories(@RequestParam String keyword) {
        return Result.success(categoryService.searchCategories(keyword));
    }

    @PostMapping("/feedback")
    public Result<Void> submitFeedback(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, Object> requestBody) {
        String feedbackType = (String) requestBody.get("feedbackType");
        Long categoryId = requestBody.get("categoryId") != null
                ? Long.valueOf(requestBody.get("categoryId").toString()) : null;
        String description = (String) requestBody.get("description");
        categoryService.submitFeedback(userId, feedbackType, categoryId, description);
        return Result.success("反馈提交成功", null);
    }

    @GetMapping("/feedback/my")
    public Result<Page<CategoryFeedback>> getMyFeedbacks(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(categoryService.getMyFeedbacks(userId, pageable));
    }
}
