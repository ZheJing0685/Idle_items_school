package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
