package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.entity.Category;
import com.idleitems.school.entity.CategoryChangeLog;
import com.idleitems.school.entity.CategoryFeedback;
import com.idleitems.school.repository.CategoryChangeLogRepository;
import com.idleitems.school.repository.CategoryFeedbackRepository;
import com.idleitems.school.repository.CategoryRepository;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.cache.CacheService;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final CategoryFeedbackRepository categoryFeedbackRepository;
    private final CategoryChangeLogRepository categoryChangeLogRepository;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;

    private static final String CATEGORIES_CACHE_KEY = "categories:all";
    private static final String CATEGORY_TREE_CACHE_KEY = "categories:tree";

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getAllCategories() {
        Object cached = cacheService.get(CATEGORIES_CACHE_KEY);
        if (cached instanceof List) {
            return (List<Map<String, Object>>) cached;
        }

        CategoryData data = loadCategoryData();
        List<Map<String, Object>> result = data.categories.stream()
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
                    map.put("itemCount", getItemCountForCategory(category.getId(), data));
                    return map;
                })
                .collect(Collectors.toList());

        cacheService.set(CATEGORIES_CACHE_KEY, result, 1800, TimeUnit.SECONDS);
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getCategoryTree() {
        Object cached = cacheService.get(CATEGORY_TREE_CACHE_KEY);
        if (cached instanceof List) {
            return (List<Map<String, Object>>) cached;
        }

        CategoryData data = loadCategoryData();

        Map<Long, Map<String, Object>> categoryMap = data.categories.stream()
                .collect(Collectors.toMap(
                        Category::getId,
                        category -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", category.getId());
                            map.put("name", category.getName());
                            map.put("icon", category.getIcon());
                            map.put("keywords", category.getKeywords());
                            map.put("itemCount", getItemCountForCategory(category.getId(), data));
                            map.put("children", new ArrayList<>());
                            return map;
                        }
                ));

        List<Map<String, Object>> tree = new ArrayList<>();
        for (Category category : data.categories) {
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

        cacheService.set(CATEGORY_TREE_CACHE_KEY, tree, 1800, TimeUnit.SECONDS);
        return tree;
    }

    private CategoryData loadCategoryData() {
        List<Category> categories = categoryRepository.findAll();
        Map<Long, List<Long>> childrenMap = buildCategoryChildrenMap(categories);

        Set<Long> allIds = categories.stream().map(Category::getId).collect(Collectors.toSet());
        List<Object[]> groupedCounts = itemRepository.countByCategoryIdsGrouped(new ArrayList<>(allIds));
        Map<Long, Long> directCountMap = new HashMap<>();
        for (Object[] row : groupedCounts) {
            Long categoryId = (Long) row[0];
            Long count = (Long) row[1];
            directCountMap.put(categoryId, count != null ? count : 0L);
        }

        return new CategoryData(categories, childrenMap, directCountMap);
    }

    private long getItemCountForCategory(Long categoryId, CategoryData data) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);
        collectAllChildCategoryIds(categoryId, data.childrenMap, ids);
        return ids.stream().mapToLong(id -> data.directCountMap.getOrDefault(id, 0L)).sum();
    }

    private static class CategoryData {
        final List<Category> categories;
        final Map<Long, List<Long>> childrenMap;
        final Map<Long, Long> directCountMap;

        CategoryData(List<Category> categories, Map<Long, List<Long>> childrenMap, Map<Long, Long> directCountMap) {
            this.categories = categories;
            this.childrenMap = childrenMap;
            this.directCountMap = directCountMap;
        }
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
            throw new BusinessException(ErrorCode.BAD_REQUEST, "分类名称不能为空");
        }

        if (category.getParentId() != null) {
            Category parent = categoryRepository.findById(category.getParentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "父分类不存在"));
            if (parent.getLevel() >= 3) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "分类层级不能超过3级");
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
            throw new BusinessException(ErrorCode.CONFLICT, "同级分类下已存在相同名称的分类");
        }

        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(true);
        }

        Category saved = categoryRepository.save(category);
        recordChangeLog(saved, CategoryChangeLog.ActionType.CREATE, operatorId, null);
        // 归一化同级分类排序值
        normalizeSortValues(category.getParentId());
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
                throw new BusinessException(ErrorCode.CONFLICT, "同级分类下已存在相同名称的分类");
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
                throw new BusinessException(ErrorCode.BAD_REQUEST, "不能将分类设置为自己的子分类");
            }
            Category newParent = categoryRepository.findById(updateData.getParentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "父分类不存在"));
            if (newParent.getLevel() >= 3) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "分类层级不能超过3级");
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
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "分类不存在"));

        // 检查子分类
        List<Category> children = categoryRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "该分类下存在" + children.size() + "个子分类，无法删除。请先删除子分类或移动到其他分类下");
        }

        // 检查直接关联物品
        Long directItemCount = itemRepository.countByCategoryId(id);
        if (directItemCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "该分类下存在" + directItemCount + "个商品，无法删除。请先将商品移至其他分类或下架商品");
        }

        // 记录变更日志
        recordChangeLog(category, CategoryChangeLog.ActionType.DELETE, operatorId, null);
        categoryRepository.deleteById(id);
        // 归一化同级分类排序值（删除后排序值可能不连续）
        normalizeSortValues(category.getParentId());
        clearCategoryCache();
        log.info("分类已删除: id={}, name={}, operator={}", id, category.getName(), operatorId);
    }

    /**
     * 批量删除分类（带安全检查）
     */
    @Transactional
    public void batchDeleteCategories(List<Long> ids, Long operatorId) {
        List<String> errors = new ArrayList<>();

        for (Long id : ids) {
            Category category = categoryRepository.findById(id).orElse(null);
            if (category == null) {
                errors.add("分类ID " + id + " 不存在");
                continue;
            }

            // 检查子分类
            List<Category> children = categoryRepository.findByParentId(id);
            if (!children.isEmpty()) {
                errors.add("分类「" + category.getName() + "」下存在" + children.size() + "个子分类，无法删除");
                continue;
            }

            // 检查关联物品
            Long itemCount = itemRepository.countByCategoryId(id);
            if (itemCount > 0) {
                errors.add("分类「" + category.getName() + "」下存在" + itemCount + "个商品，无法删除");
                continue;
            }

            recordChangeLog(category, CategoryChangeLog.ActionType.DELETE, operatorId, null);
            categoryRepository.deleteById(id);
        }

        if (!errors.isEmpty()) {
            log.warn("批量删除分类部分失败: {}", errors);
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "以下分类无法删除: " + String.join("; ", errors));
        }

        clearCategoryCache();
        log.info("批量删除分类完成: ids={}, operator={}", ids, operatorId);
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
                throw new BusinessException(ErrorCode.BAD_REQUEST, "文件为空");
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
        cacheService.delete(CATEGORIES_CACHE_KEY);
        cacheService.delete(CATEGORY_TREE_CACHE_KEY);
    }

    /**
     * 归一化同级分类的排序值
     * 确保同级分类的排序值连续且唯一（从0开始递增）
     */
    public void normalizeSortValues(Long parentId) {
        List<Category> siblings;
        if (parentId != null) {
            siblings = categoryRepository.findByParentId(parentId);
        } else {
            siblings = categoryRepository.findByParentIdIsNull();
        }

        siblings.sort(Comparator.comparing(c -> c.getSort() != null ? c.getSort() : 0));

        boolean changed = false;
        for (int i = 0; i < siblings.size(); i++) {
            Category sibling = siblings.get(i);
            if (sibling.getSort() == null || sibling.getSort() != i) {
                sibling.setSort(i);
                categoryRepository.save(sibling);
                changed = true;
            }
        }

        if (changed) {
            log.debug("已归一化分类排序: parentId={}, 数量={}", parentId, siblings.size());
        }
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
