package com.idleitems.school.service;

import com.idleitems.school.entity.Category;
import com.idleitems.school.entity.CategoryChangeLog;
import com.idleitems.school.entity.CategoryFeedback;
import com.idleitems.school.repository.CategoryChangeLogRepository;
import com.idleitems.school.repository.CategoryFeedbackRepository;
import com.idleitems.school.repository.CategoryRepository;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.util.CacheManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final CategoryFeedbackRepository categoryFeedbackRepository;
    private final CategoryChangeLogRepository categoryChangeLogRepository;
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    private static final String CATEGORIES_CACHE_KEY = "categories:all";
    private static final String CATEGORY_TREE_CACHE_KEY = "categories:tree";

    public List<Map<String, Object>> getAllCategories() {
        try {
            Object cached = cacheManager.get(CATEGORIES_CACHE_KEY);
            if (cached instanceof List) {
                return (List<Map<String, Object>>) cached;
            }
        } catch (Exception e) {
            log.error("获取分类缓存失败: {}", e.getMessage());
        }

        List<Category> categories = categoryRepository.findAll();
        Map<Long, List<Long>> categoryChildrenMap = buildCategoryChildrenMap(categories);

        List<Map<String, Object>> result = categories.stream()
                .map(category -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", category.getId());
                    map.put("name", category.getName());
                    map.put("description", category.getDescription());
                    map.put("parentId", category.getParentId());
                    map.put("sortOrder", category.getSort());
                    map.put("icon", category.getIcon());
                    map.put("createdAt", category.getCreatedAt());
                    map.put("updatedAt", category.getUpdatedAt());
                    map.put("itemCount", calculateItemCount(category.getId(), categoryChildrenMap));
                    return map;
                })
                .collect(Collectors.toList());

        try {
            cacheManager.set(CATEGORIES_CACHE_KEY, result, 1800);
        } catch (Exception e) {
            log.error("设置分类缓存失败: {}", e.getMessage());
        }
        return result;
    }

    public List<Map<String, Object>> getCategoryTree() {
        try {
            Object cached = cacheManager.get(CATEGORY_TREE_CACHE_KEY);
            if (cached instanceof List) {
                return (List<Map<String, Object>>) cached;
            }
        } catch (Exception e) {
            log.error("获取分类树缓存失败: {}", e.getMessage());
        }

        List<Category> categories = categoryRepository.findAll();
        Map<Long, List<Long>> categoryChildrenMap = buildCategoryChildrenMap(categories);

        Map<Long, Map<String, Object>> categoryMap = categories.stream()
                .collect(Collectors.toMap(
                        Category::getId,
                        category -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", category.getId());
                            map.put("name", category.getName());
                            map.put("icon", category.getIcon());
                            map.put("keywords", category.getKeywords());
                            map.put("itemCount", calculateItemCount(category.getId(), categoryChildrenMap));
                            map.put("children", new ArrayList<>());
                            return map;
                        }
                ));

        List<Map<String, Object>> tree = new ArrayList<>();
        for (Category category : categories) {
            if (category.getParentId() == null) {
                tree.add(categoryMap.get(category.getId()));
            } else {
                Map<String, Object> parent = categoryMap.get(category.getParentId());
                if (parent != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
                    children.add(categoryMap.get(category.getId()));
                }
            }
        }

        try {
            cacheManager.set(CATEGORY_TREE_CACHE_KEY, tree, 1800);
        } catch (Exception e) {
            log.error("设置分类树缓存失败: {}", e.getMessage());
        }
        return tree;
    }

    public List<Map<String, Object>> searchCategories(String keyword) {
        List<Category> categories = categoryRepository.findAll();
        String lowerKeyword = keyword.toLowerCase();
        return categories.stream()
                .filter(c -> (c.getName() != null && c.getName().toLowerCase().contains(lowerKeyword))
                        || (c.getKeywords() != null && c.getKeywords().toLowerCase().contains(lowerKeyword)))
                .map(category -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", category.getId());
                    map.put("name", category.getName());
                    map.put("icon", category.getIcon());
                    map.put("parentId", category.getParentId());
                    map.put("level", category.getLevel());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Category createCategory(Category category, Long operatorId) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }

        if (category.getParentId() != null) {
            Category parent = categoryRepository.findById(category.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("父分类不存在"));
            if (parent.getLevel() >= 3) {
                throw new IllegalArgumentException("分类层级不能超过3级");
            }
            category.setLevel(parent.getLevel() + 1);
        } else {
            category.setLevel(1);
        }

        List<Category> siblings = category.getParentId() != null
                ? categoryRepository.findByParentId(category.getParentId())
                : categoryRepository.findByParentIdIsNull();
        boolean nameExists = siblings.stream()
                .anyMatch(c -> c.getName().equals(category.getName()));
        if (nameExists) {
            throw new IllegalArgumentException("同级分类下已存在相同名称的分类");
        }

        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(true);
        }

        Category saved = categoryRepository.save(category);
        recordChangeLog(saved, CategoryChangeLog.ActionType.CREATE, operatorId, null);
        clearCategoryCache();
        return saved;
    }

    @Transactional
    public Category updateCategory(Long id, Category updateData, Long operatorId) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在"));

        Map<String, Object> changes = new HashMap<>();

        if (updateData.getName() != null && !updateData.getName().equals(existing.getName())) {
            List<Category> siblings = existing.getParentId() != null
                    ? categoryRepository.findByParentId(existing.getParentId())
                    : categoryRepository.findByParentIdIsNull();
            boolean nameExists = siblings.stream()
                    .anyMatch(c -> !c.getId().equals(id) && c.getName().equals(updateData.getName()));
            if (nameExists) {
                throw new IllegalArgumentException("同级分类下已存在相同名称的分类");
            }
            changes.put("name", Map.of("old", existing.getName(), "new", updateData.getName()));
            existing.setName(updateData.getName());
        }

        if (updateData.getDescription() != null) {
            changes.put("description", Map.of(
                    "old", existing.getDescription() != null ? existing.getDescription() : "",
                    "new", updateData.getDescription()));
            existing.setDescription(updateData.getDescription());
        }

        if (updateData.getIcon() != null) {
            changes.put("icon", Map.of(
                    "old", existing.getIcon() != null ? existing.getIcon() : "",
                    "new", updateData.getIcon()));
            existing.setIcon(updateData.getIcon());
        }

        if (updateData.getSort() != null) {
            changes.put("sort", Map.of("old", existing.getSort(), "new", updateData.getSort()));
            existing.setSort(updateData.getSort());
        }

        if (updateData.getKeywords() != null) {
            changes.put("keywords", Map.of(
                    "old", existing.getKeywords() != null ? existing.getKeywords() : "",
                    "new", updateData.getKeywords()));
            existing.setKeywords(updateData.getKeywords());
        }

        if (updateData.getParentId() != null && !updateData.getParentId().equals(existing.getParentId())) {
            if (updateData.getParentId().equals(id)) {
                throw new IllegalArgumentException("不能将分类设置为自己的子分类");
            }
            Category newParent = categoryRepository.findById(updateData.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("父分类不存在"));
            if (newParent.getLevel() >= 3) {
                throw new IllegalArgumentException("分类层级不能超过3级");
            }
            changes.put("parentId", Map.of("old", existing.getParentId(), "new", updateData.getParentId()));
            existing.setParentId(updateData.getParentId());
            existing.setLevel(newParent.getLevel() + 1);
        }

        existing.setUpdatedBy(operatorId);
        Category saved = categoryRepository.save(existing);

        if (!changes.isEmpty()) {
            recordChangeLog(saved, CategoryChangeLog.ActionType.UPDATE, operatorId, changes);
        }
        clearCategoryCache();
        return saved;
    }

    @Transactional
    public void deleteCategory(Long id, Long operatorId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在"));

        List<Category> children = categoryRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new IllegalArgumentException("该分类下存在子分类，无法删除");
        }

        Long itemCount = itemRepository.countByCategoryId(id);
        if (itemCount > 0) {
            throw new IllegalArgumentException("该分类下存在商品，无法删除");
        }

        recordChangeLog(category, CategoryChangeLog.ActionType.DELETE, operatorId, null);
        categoryRepository.deleteById(id);
        clearCategoryCache();
    }

    @Transactional
    public Category toggleCategoryStatus(Long id, Boolean status, Long operatorId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在"));

        Map<String, Object> changes = Map.of("status", Map.of("old", category.getStatus(), "new", status));
        category.setStatus(status);
        category.setUpdatedBy(operatorId);
        Category saved = categoryRepository.save(category);

        recordChangeLog(saved, CategoryChangeLog.ActionType.STATUS_CHANGE, operatorId, changes);
        clearCategoryCache();
        return saved;
    }

    @Transactional
    public void submitFeedback(Long userId, String feedbackType, Long categoryId, String description) {
        CategoryFeedback feedback = new CategoryFeedback();
        feedback.setUserId(userId);
        feedback.setFeedbackType(CategoryFeedback.FeedbackType.valueOf(feedbackType));
        feedback.setCategoryId(categoryId);
        feedback.setDescription(description);
        feedback.setStatus(CategoryFeedback.FeedbackStatus.PENDING);
        categoryFeedbackRepository.save(feedback);
    }

    public Page<CategoryFeedback> getMyFeedbacks(Long userId, Pageable pageable) {
        return categoryFeedbackRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Page<CategoryFeedback> getAllFeedbacks(String status, Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            return categoryFeedbackRepository.findByStatus(
                    CategoryFeedback.FeedbackStatus.valueOf(status), pageable);
        }
        return categoryFeedbackRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional
    public CategoryFeedback reviewFeedback(Long feedbackId, String action, String reply, Long adminId) {
        CategoryFeedback feedback = categoryFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("反馈记录不存在"));

        feedback.setStatus(CategoryFeedback.FeedbackStatus.valueOf(action));
        feedback.setAdminReply(reply);
        feedback.setReviewedBy(adminId);
        feedback.setReviewedAt(LocalDateTime.now());
        return categoryFeedbackRepository.save(feedback);
    }

    public Page<CategoryChangeLog> getCategoryChangeLogs(Long categoryId, Pageable pageable) {
        if (categoryId != null) {
            return categoryChangeLogRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
        }
        return categoryChangeLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Map<String, Object> getCategoryStats() {
        long total = categoryRepository.count();
        List<Category> all = categoryRepository.findAll();
        long active = all.stream().filter(Category::getStatus).count();
        long level1 = all.stream().filter(c -> c.getLevel() != null && c.getLevel() == 1).count();
        long level2 = all.stream().filter(c -> c.getLevel() != null && c.getLevel() == 2).count();

        Map<Long, List<Long>> childrenMap = buildCategoryChildrenMap(all);
        List<Map<String, Object>> categoryItemCounts = all.stream()
                .filter(c -> c.getLevel() != null && c.getLevel() == 1)
                .map(c -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", c.getId());
                    item.put("name", c.getName());
                    item.put("itemCount", calculateItemCount(c.getId(), childrenMap));
                    return item;
                })
                .collect(Collectors.toList());

        long pendingFeedbacks = categoryFeedbackRepository.countByStatus(CategoryFeedback.FeedbackStatus.PENDING);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("active", active);
        stats.put("level1", level1);
        stats.put("level2", level2);
        stats.put("categoryItemCounts", categoryItemCounts);
        stats.put("pendingFeedbacks", pendingFeedbacks);
        return stats;
    }

    public String exportCategories() {
        List<Category> categories = categoryRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("id,name,parentId,level,sort,status,icon,description,keywords\n");
        for (Category c : categories) {
            sb.append(c.getId()).append(",")
              .append(escapeCsv(c.getName())).append(",")
              .append(c.getParentId() != null ? c.getParentId() : "").append(",")
              .append(c.getLevel() != null ? c.getLevel() : 1).append(",")
              .append(c.getSort() != null ? c.getSort() : 0).append(",")
              .append(c.getStatus() != null && c.getStatus() ? "ACTIVE" : "DISABLED").append(",")
              .append(escapeCsv(c.getIcon())).append(",")
              .append(escapeCsv(c.getDescription())).append(",")
              .append(escapeCsv(c.getKeywords())).append("\n");
        }
        return sb.toString();
    }

    @Transactional
    public Map<String, Object> importCategories(MultipartFile file, Long operatorId) {
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null) {
                throw new IllegalArgumentException("文件为空");
            }

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                try {
                    String[] parts = parseCsvLine(line);
                    if (parts.length < 2) {
                        errors.add("第" + lineNum + "行: 数据列数不足");
                        failCount++;
                        continue;
                    }

                    Category category = new Category();
                    category.setName(parts[1].trim());

                    if (parts.length > 2 && !parts[2].trim().isEmpty()) {
                        category.setParentId(Long.parseLong(parts[2].trim()));
                    }
                    if (parts.length > 3 && !parts[3].trim().isEmpty()) {
                        category.setLevel(Integer.parseInt(parts[3].trim()));
                    }
                    if (parts.length > 4 && !parts[4].trim().isEmpty()) {
                        category.setSort(Integer.parseInt(parts[4].trim()));
                    }
                    if (parts.length > 5) {
                        category.setStatus("ACTIVE".equalsIgnoreCase(parts[5].trim()));
                    }
                    if (parts.length > 6) {
                        category.setIcon(parts[6].trim());
                    }
                    if (parts.length > 7) {
                        category.setDescription(parts[7].trim());
                    }
                    if (parts.length > 8) {
                        category.setKeywords(parts[8].trim());
                    }

                    if (category.getLevel() == null) {
                        category.setLevel(category.getParentId() != null ? 2 : 1);
                    }
                    if (category.getSort() == null) {
                        category.setSort(0);
                    }
                    if (category.getStatus() == null) {
                        category.setStatus(true);
                    }

                    categoryRepository.save(category);
                    recordChangeLog(category, CategoryChangeLog.ActionType.CREATE, operatorId, null);
                    successCount++;
                } catch (Exception e) {
                    errors.add("第" + lineNum + "行: " + e.getMessage());
                    failCount++;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("读取文件失败: " + e.getMessage());
        }

        clearCategoryCache();

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        return result;
    }

    private void recordChangeLog(Category category, CategoryChangeLog.ActionType action, Long operatorId, Map<String, Object> changes) {
        try {
            CategoryChangeLog changeLog = new CategoryChangeLog();
            changeLog.setCategoryId(category.getId());
            changeLog.setCategoryName(category.getName());
            changeLog.setAction(action);
            changeLog.setOperatorId(operatorId);
            if (changes != null) {
                changeLog.setDetails(objectMapper.writeValueAsString(changes));
            }
            categoryChangeLogRepository.save(changeLog);
        } catch (Exception e) {
            log.error("记录分类变更日志失败: {}", e.getMessage());
        }
    }

    private void clearCategoryCache() {
        cacheManager.delete(CATEGORIES_CACHE_KEY);
        cacheManager.delete(CATEGORY_TREE_CACHE_KEY);
    }

    private Map<Long, List<Long>> buildCategoryChildrenMap(List<Category> categories) {
        Map<Long, List<Long>> childrenMap = new HashMap<>();
        for (Category category : categories) {
            if (category.getParentId() != null) {
                childrenMap.computeIfAbsent(category.getParentId(), k -> new ArrayList<>())
                        .add(category.getId());
            }
        }
        return childrenMap;
    }

    private Long calculateItemCount(Long categoryId, Map<Long, List<Long>> categoryChildrenMap) {
        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(categoryId);
        collectAllChildCategoryIds(categoryId, categoryChildrenMap, categoryIds);
        return itemRepository.countByCategoryIds(categoryIds);
    }

    private void collectAllChildCategoryIds(Long categoryId, Map<Long, List<Long>> categoryChildrenMap, List<Long> result) {
        List<Long> children = categoryChildrenMap.get(categoryId);
        if (children != null) {
            for (Long childId : children) {
                result.add(childId);
                collectAllChildCategoryIds(childId, categoryChildrenMap, result);
            }
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}
