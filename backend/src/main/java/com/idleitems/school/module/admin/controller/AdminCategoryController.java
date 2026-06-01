package com.idleitems.school.module.admin.controller;

import com.idleitems.school.common.annotation.RequireRole;
import com.idleitems.school.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.idleitems.school.module.category.dto.CategoryDTO;
import com.idleitems.school.module.category.entity.Category;
import com.idleitems.school.module.category.entity.CategoryChangeLog;
import com.idleitems.school.module.category.entity.CategoryFeedback;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.category.repository.CategoryRepository;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.admin.service.AdminLogService;
import com.idleitems.school.module.category.service.CategoryQueryService;
import com.idleitems.school.module.category.service.CategoryCommandService;
import com.idleitems.school.module.category.service.CategoryFeedbackService;
import com.idleitems.school.config.ApiPaths;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(ApiPaths.Admin.CATEGORIES)
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN}, message = "需要管理员权限")
@Tag(name = "管理员-分类管理", description = "管理员分类管理相关接口")
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final CategoryCommandService categoryCommandService;
    private final CategoryFeedbackService categoryFeedbackService;
    private final CategoryQueryService categoryQueryService;
    private final AdminLogService adminLogService;

    @GetMapping
    @Operation(summary = "获取分类列表", description = "分页查询所有分类，支持按状态筛选")
    public Result<Page<Map<String, Object>>> getCategories(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) Boolean status) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "sort"));
        Page<Category> categories;
        if (status != null) {
            categories = categoryRepository.findByStatus(status, pageable);
        } else {
            categories = categoryRepository.findAll(pageable);
        }

        Page<Map<String, Object>> result = categories.map(category -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", category.getId());
            map.put("name", category.getName());
            map.put("description", category.getDescription());
            map.put("parentId", category.getParentId());
            map.put("level", category.getLevel());
            map.put("sort", category.getSort());
            map.put("status", category.getStatus());
            map.put("createdAt", category.getCreatedAt());
            map.put("updatedAt", category.getUpdatedAt());
            List<Long> categoryIds = new ArrayList<>();
            categoryIds.add(category.getId());
            collectChildCategories(category.getId(), categoryIds);
            map.put("itemCount", itemRepository.countByCategoryIds(categoryIds));
            return map;
        });
        return Result.success(result);
    }

    @GetMapping("/stats")
    @Operation(summary = "获取分类统计", description = "获取分类的统计数据")
    public Result<Map<String, Object>> getCategoryStats() {
        return Result.success(categoryQueryService.getCategoryStats());
    }

    private void collectChildCategories(Long categoryId, List<Long> categoryIds) {
        List<Category> children = categoryRepository.findByParentId(categoryId);
        for (Category child : children) {
            categoryIds.add(child.getId());
            collectChildCategories(child.getId(), categoryIds);
        }
    }

    @PostMapping
    @Operation(summary = "创建分类", description = "管理员创建新的分类")
    public Result<CategoryDTO> createCategory(
            @RequestAttribute("userId") Long adminId,
            @RequestBody Category category,
            HttpServletRequest request) {
        Category saved = categoryCommandService.createCategory(category, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("categoryId", saved.getId());
        details.put("categoryName", saved.getName());
        adminLogService.logOperation(adminId, "创建分类", "CATEGORY", saved.getId(), details, request);

        return Result.success("分类创建成功", CategoryDTO.fromEntity(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新分类", description = "管理员更新指定分类的信息")
    public Result<CategoryDTO> updateCategory(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestBody Category category,
            HttpServletRequest request) {
        Category saved = categoryCommandService.updateCategory(id, category, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("categoryId", id);
        details.put("categoryName", saved.getName());
        adminLogService.logOperation(adminId, "更新分类", "CATEGORY", id, details, request);

        return Result.success("分类更新成功", CategoryDTO.fromEntity(saved));
    }

    @PostMapping("/{id}/move-up")
    @Operation(summary = "分类排序上移", description = "将指定分类的排序位置上移")
    public Result<Void> moveCategoryUp(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        Category category = categoryRepository.findById(id.longValue())
                .orElseThrow(() -> new IllegalArgumentException("分类不存在"));

        Integer currentSort = category.getSort();
        if (currentSort == null) {
            currentSort = 0;
        }

        Page<Category> nextPage = categoryRepository.findBySortGreaterThan(currentSort, PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "sort")));
        if (nextPage.hasContent()) {
            Category nextCategory = nextPage.getContent().get(0);
            Integer nextSort = nextCategory.getSort();
            nextCategory.setSort(currentSort);
            category.setSort(nextSort != null ? nextSort : currentSort + 1);
            categoryRepository.save(nextCategory);
            categoryRepository.save(category);
        } else {
            category.setSort(currentSort + 1);
            categoryRepository.save(category);
        }

        // 归一化排序值确保连续
        categoryCommandService.normalizeSortValues(category.getParentId());

        Map<String, Object> details = new HashMap<>();
        details.put("categoryId", id);
        details.put("action", "move-up");
        adminLogService.logOperation(adminId, "分类排序上移", "CATEGORY", id, details, request);

        return Result.success("排序已更新", null);
    }

    @PostMapping("/{id}/move-down")
    @Operation(summary = "分类排序下移", description = "将指定分类的排序位置下移")
    public Result<Void> moveCategoryDown(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        Category category = categoryRepository.findById(id.longValue())
                .orElseThrow(() -> new IllegalArgumentException("分类不存在"));

        Integer currentSort = category.getSort();
        if (currentSort == null) {
            currentSort = 0;
        }

        Page<Category> prevPage = categoryRepository.findBySortLessThan(currentSort, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "sort")));
        if (prevPage.hasContent()) {
            Category prevCategory = prevPage.getContent().get(0);
            Integer prevSort = prevCategory.getSort();
            prevCategory.setSort(currentSort);
            category.setSort(prevSort != null ? prevSort : currentSort - 1);
            categoryRepository.save(prevCategory);
            categoryRepository.save(category);
        } else {
            if (currentSort > 0) {
                category.setSort(currentSort - 1);
                categoryRepository.save(category);
            }
        }

        // 归一化排序值确保连续
        categoryCommandService.normalizeSortValues(category.getParentId());

        Map<String, Object> details = new HashMap<>();
        details.put("categoryId", id);
        details.put("action", "move-down");
        adminLogService.logOperation(adminId, "分类排序下移", "CATEGORY", id, details, request);

        return Result.success("排序已更新", null);
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "更新分类状态", description = "启用或禁用指定分类")
    public Result<CategoryDTO> updateCategoryStatus(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        Boolean status = (Boolean) requestBody.get("status");
        if (status == null) {
            throw new IllegalArgumentException("状态参数不能为空");
        }

        Category saved = categoryCommandService.toggleCategoryStatus(id, status, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("categoryId", id);
        details.put("newStatus", status);
        adminLogService.logOperation(adminId, "更新分类状态", "CATEGORY", id, details, request);

        return Result.success("状态更新成功", CategoryDTO.fromEntity(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类", description = "根据ID删除指定分类")
    public Result<Void> deleteCategory(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        categoryCommandService.deleteCategory(id, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("categoryId", id);
        adminLogService.logOperation(adminId, "删除分类", "CATEGORY", id, details, request);

        return Result.success("分类删除成功", null);
    }

    @GetMapping("/feedback")
    @Operation(summary = "获取分类反馈列表", description = "分页查询用户提交的分类反馈")
    public Result<Page<CategoryFeedback>> getCategoryFeedbacks(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(categoryFeedbackService.getAllFeedbacks(status, pageable));
    }

    @PostMapping("/feedback/{id}/review")
    @Operation(summary = "审核分类反馈", description = "审核用户提交的分类反馈")
    public Result<CategoryFeedback> reviewFeedback(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        String action = (String) requestBody.get("action");
        String reply = (String) requestBody.get("reply");
        CategoryFeedback feedback = categoryFeedbackService.reviewFeedback(id, action, reply, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("feedbackId", id);
        details.put("action", action);
        adminLogService.logOperation(adminId, "审核分类反馈", "CATEGORY_FEEDBACK", id, details, request);

        return Result.success("反馈审核成功", feedback);
    }

    @GetMapping("/change-logs")
    @Operation(summary = "获取分类变更日志", description = "查询分类的变更历史记录")
    public Result<Page<CategoryChangeLog>> getCategoryChangeLogs(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(categoryQueryService.getCategoryChangeLogs(categoryId, pageable));
    }

    @GetMapping("/export")
    @Operation(summary = "导出分类", description = "导出分类列表为CSV文件")
    public ResponseEntity<byte[]> exportCategories() {
        String csv = categoryQueryService.exportCategories();
        // 添加 UTF-8 BOM 以确保 Excel 正确识别编码
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] csvBytes = csv.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = new byte[bom.length + csvBytes.length];
        System.arraycopy(bom, 0, bytes, 0, bom.length);
        System.arraycopy(csvBytes, 0, bytes, bom.length, csvBytes.length);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=categories.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(bytes);
    }

    @PostMapping("/import")
    @Operation(summary = "导入分类", description = "通过CSV文件批量导入分类")
    public Result<Map<String, Object>> importCategories(
            @RequestAttribute("userId") Long adminId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        Map<String, Object> result = categoryCommandService.importCategories(file, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("successCount", result.get("successCount"));
        details.put("failCount", result.get("failCount"));
        adminLogService.logOperation(adminId, "批量导入分类", "CATEGORY", null, details, request);

        return Result.success("导入完成", result);
    }

    @PostMapping("/batch/enable")
    @Operation(summary = "批量启用分类", description = "批量启用指定ID列表的分类")
    @Transactional
    public Result<Void> batchEnableCategories(
            @RequestAttribute("userId") Long adminId,
            @RequestBody List<Long> categoryIds,
            HttpServletRequest request) {
        for (Long id : categoryIds) {
            Category category = categoryRepository.findById(id.longValue())
                    .orElseThrow(() -> new IllegalArgumentException("分类不存在"));
            category.setStatus(true);
            categoryRepository.save(category);

            Map<String, Object> details = new HashMap<>();
            details.put("categoryId", id);
            details.put("categoryName", category.getName());
            details.put("action", "enable");
            adminLogService.logOperation(adminId, "批量启用分类", "CATEGORY", id, details, request);
        }
        return Result.success("批量启用成功", null);
    }

    @PostMapping("/batch/disable")
    @Operation(summary = "批量禁用分类", description = "批量禁用指定ID列表的分类")
    @Transactional
    public Result<Void> batchDisableCategories(
            @RequestAttribute("userId") Long adminId,
            @RequestBody List<Long> categoryIds,
            HttpServletRequest request) {
        for (Long id : categoryIds) {
            Category category = categoryRepository.findById(id.longValue())
                    .orElseThrow(() -> new IllegalArgumentException("分类不存在"));
            category.setStatus(false);
            categoryRepository.save(category);

            Map<String, Object> details = new HashMap<>();
            details.put("categoryId", id);
            details.put("categoryName", category.getName());
            details.put("action", "disable");
            adminLogService.logOperation(adminId, "批量禁用分类", "CATEGORY", id, details, request);
        }
        return Result.success("批量禁用成功", null);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除分类", description = "批量删除指定ID列表的分类（会检查关联物品和子分类）")
    public Result<Void> batchDeleteCategories(
            @RequestAttribute("userId") Long adminId,
            @RequestBody List<Long> categoryIds,
            HttpServletRequest request) {
        categoryCommandService.batchDeleteCategories(categoryIds, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("categoryIds", categoryIds);
        adminLogService.logOperation(adminId, "批量删除分类", "CATEGORY", null, details, request);

        return Result.success("批量删除成功", null);
    }
}
