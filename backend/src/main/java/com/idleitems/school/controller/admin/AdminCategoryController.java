package com.idleitems.school.controller.admin;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.common.Result;
import com.idleitems.school.dto.CategoryDTO;
import com.idleitems.school.entity.Category;
import com.idleitems.school.entity.CategoryChangeLog;
import com.idleitems.school.entity.CategoryFeedback;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.CategoryRepository;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.service.AdminLogService;
import com.idleitems.school.service.CategoryService;
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
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN}, message = "需要管理员权限")
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final CategoryService categoryService;
    private final AdminLogService adminLogService;

    @GetMapping
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
    public Result<Map<String, Object>> getCategoryStats() {
        return Result.success(categoryService.getCategoryStats());
    }

    private void collectChildCategories(Long categoryId, List<Long> categoryIds) {
        List<Category> children = categoryRepository.findByParentId(categoryId);
        for (Category child : children) {
            categoryIds.add(child.getId());
            collectChildCategories(child.getId(), categoryIds);
        }
    }

    @PostMapping
    public Result<CategoryDTO> createCategory(
            @RequestAttribute("userId") Long adminId,
            @RequestBody Category category,
            HttpServletRequest request) {
        Category saved = categoryService.createCategory(category, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("categoryId", saved.getId());
        details.put("categoryName", saved.getName());
        adminLogService.logOperation(adminId, "创建分类", "CATEGORY", saved.getId(), details, request);

        return Result.success("分类创建成功", CategoryDTO.fromEntity(saved));
    }

    @PutMapping("/{id}")
    public Result<CategoryDTO> updateCategory(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestBody Category category,
            HttpServletRequest request) {
        Category saved = categoryService.updateCategory(id, category, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("categoryId", id);
        details.put("categoryName", saved.getName());
        adminLogService.logOperation(adminId, "更新分类", "CATEGORY", id, details, request);

        return Result.success("分类更新成功", CategoryDTO.fromEntity(saved));
    }

    @PutMapping("/{id}/move-up")
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

        Map<String, Object> details = new HashMap<>();
        details.put("categoryId", id);
        details.put("action", "move-up");
        adminLogService.logOperation(adminId, "分类排序上移", "CATEGORY", id, details, request);

        return Result.success("排序已更新", null);
    }

    @PutMapping("/{id}/move-down")
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

        Map<String, Object> details = new HashMap<>();
        details.put("categoryId", id);
        details.put("action", "move-down");
        adminLogService.logOperation(adminId, "分类排序下移", "CATEGORY", id, details, request);

        return Result.success("排序已更新", null);
    }

    @PutMapping("/{id}/status")
    public Result<CategoryDTO> updateCategoryStatus(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        Boolean status = (Boolean) requestBody.get("status");
        if (status == null) {
            throw new IllegalArgumentException("状态参数不能为空");
        }

        Category saved = categoryService.toggleCategoryStatus(id, status, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("categoryId", id);
        details.put("newStatus", status);
        adminLogService.logOperation(adminId, "更新分类状态", "CATEGORY", id, details, request);

        return Result.success("状态更新成功", CategoryDTO.fromEntity(saved));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        categoryService.deleteCategory(id, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("categoryId", id);
        adminLogService.logOperation(adminId, "删除分类", "CATEGORY", id, details, request);

        return Result.success("分类删除成功", null);
    }

    @GetMapping("/feedback")
    public Result<Page<CategoryFeedback>> getCategoryFeedbacks(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(categoryService.getAllFeedbacks(status, pageable));
    }

    @PutMapping("/feedback/{id}/review")
    public Result<CategoryFeedback> reviewFeedback(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        String action = (String) requestBody.get("action");
        String reply = (String) requestBody.get("reply");
        CategoryFeedback feedback = categoryService.reviewFeedback(id, action, reply, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("feedbackId", id);
        details.put("action", action);
        adminLogService.logOperation(adminId, "审核分类反馈", "CATEGORY_FEEDBACK", id, details, request);

        return Result.success("反馈审核成功", feedback);
    }

    @GetMapping("/change-logs")
    public Result<Page<CategoryChangeLog>> getCategoryChangeLogs(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(categoryService.getCategoryChangeLogs(categoryId, pageable));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCategories() {
        String csv = categoryService.exportCategories();
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=categories.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(bytes);
    }

    @PostMapping("/import")
    public Result<Map<String, Object>> importCategories(
            @RequestAttribute("userId") Long adminId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        Map<String, Object> result = categoryService.importCategories(file, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("successCount", result.get("successCount"));
        details.put("failCount", result.get("failCount"));
        adminLogService.logOperation(adminId, "批量导入分类", "CATEGORY", null, details, request);

        return Result.success("导入完成", result);
    }

    @PutMapping("/batch/enable")
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

    @PutMapping("/batch/disable")
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
    @Transactional
    public Result<Void> batchDeleteCategories(
            @RequestAttribute("userId") Long adminId,
            @RequestBody List<Long> categoryIds,
            HttpServletRequest request) {
        for (Long id : categoryIds) {
            Category category = categoryRepository.findById(id.longValue())
                    .orElseThrow(() -> new IllegalArgumentException("分类不存在"));
            
            Map<String, Object> details = new HashMap<>();
            details.put("categoryId", id);
            details.put("categoryName", category.getName());
            adminLogService.logOperation(adminId, "批量删除分类", "CATEGORY", id, details, request);
            
            categoryRepository.delete(category);
        }
        return Result.success("批量删除成功", null);
    }
}
