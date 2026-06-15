package com.idleitems.school.module.category.service;

import com.idleitems.school.module.category.entity.Category;
import com.idleitems.school.module.category.entity.CategoryChangeLog;
import com.idleitems.school.module.category.entity.CategoryFeedback;
import com.idleitems.school.module.category.repository.CategoryChangeLogRepository;
import com.idleitems.school.module.category.repository.CategoryFeedbackRepository;
import com.idleitems.school.module.category.repository.CategoryRepository;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.shared.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final CategoryFeedbackRepository categoryFeedbackRepository;
    private final CategoryChangeLogRepository categoryChangeLogRepository;
    private final CacheService cacheService;

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getAllCategories() {
        Object cached = cacheService.get("categories:all");
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

        cacheService.set("categories:all", result, 3600, TimeUnit.SECONDS);
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getCategoryTree() {
        Object cached = cacheService.get("categories:tree");
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

        cacheService.set("categories:tree", tree, 3600, TimeUnit.SECONDS);
        return tree;
    }

    public List<Map<String, Object>> getChildren(Long parentId) {
        List<Category> children = categoryRepository.findByParentId(parentId);
        if (children.isEmpty()) {
            return Collections.emptyList();
        }
        CategoryData data = loadCategoryData();
        return children.stream()
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("name", c.getName());
                    map.put("icon", c.getIcon());
                    map.put("parentId", c.getParentId());
                    map.put("sort", c.getSort());
                    map.put("itemCount", getItemCountForCategory(c.getId(), data));
                    return map;
                })
                .sorted((a, b) -> {
                    int sortA = a.get("sort") instanceof Integer ? (Integer) a.get("sort") : 0;
                    int sortB = b.get("sort") instanceof Integer ? (Integer) b.get("sort") : 0;
                    int cmp = Integer.compare(sortA, sortB);
                    if (cmp != 0) return cmp;
                    String nameA = (String) a.getOrDefault("name", "");
                    String nameB = (String) b.getOrDefault("name", "");
                    return nameA.compareTo(nameB);
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> suggestCategories(String prefix) {
        List<Category> matches = categoryRepository.searchByKeyword(prefix);
        return matches.stream()
                .limit(5)
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("name", c.getName());
                    map.put("icon", c.getIcon());
                    map.put("parentName", getParentName(c));
                    map.put("level", c.getLevel());
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> recommendCategories(String title, int limit) {
        String[] tokens = title.split("[\\s,，。、/]+");
        
        List<Category> allActive = categoryRepository.findByStatus(true);
        Set<Long> parentIds = allActive.stream()
                .map(Category::getParentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Map<String, Object>> results = allActive.stream()
                .filter(c -> !parentIds.contains(c.getId()))
                .map(c -> {
                    double score = 0;
                    String matchedToken = "";
                    for (String token : tokens) {
                        if (token.isEmpty()) continue;
                        String lowerToken = token.toLowerCase();
                        if (c.getName() != null && c.getName().toLowerCase().contains(lowerToken)) {
                            score += 0.5;
                            matchedToken = token;
                        }
                        if (c.getKeywords() != null &&
                            c.getKeywords().toLowerCase().contains(lowerToken)) {
                            score += 0.3;
                            if (matchedToken.isEmpty()) matchedToken = token;
                        }
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("name", c.getName());
                    map.put("icon", c.getIcon());
                    map.put("parentName", getParentName(c));
                    map.put("score", Math.min(score, 1.0));
                    map.put("matchedToken", matchedToken);
                    return map;
                })
                .filter(m -> (Double) m.get("score") > 0)
                .sorted((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")))
                .limit(limit)
                .collect(Collectors.toList());

        return results;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getHotCategories(int limit) {
        Object cached = cacheService.get("categories:hot");
        if (cached instanceof List) {
            List<Map<String, Object>> cachedResult = (List<Map<String, Object>>) cached;
            return cachedResult.stream().limit(limit).collect(Collectors.toList());
        }

        List<Category> allActive = categoryRepository.findByStatus(true);
        Map<Long, Category> categoryMap = allActive.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));
        List<Object[]> counts = itemRepository.countItemsByCategory();

        Map<Long, Long> countMap = new HashMap<>();
        for (Object[] row : counts) {
            Long categoryId = (Long) row[0];
            Long count = (Long) row[1];
            countMap.put(categoryId, count != null ? count : 0L);
        }

        List<Map<String, Object>> result = countMap.entrySet().stream()
                .map(entry -> {
                    Long categoryId = entry.getKey();
                    Long itemCount = entry.getValue();
                    Category c = categoryMap.get(categoryId);
                    if (c == null) return null;
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("name", c.getName());
                    map.put("icon", c.getIcon());
                    map.put("itemCount", itemCount);
                    map.put("parentName", getParentName(c));
                    return map;
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> Long.compare((Long) b.get("itemCount"), (Long) a.get("itemCount")))
                .limit(limit)
                .collect(Collectors.toList());

        cacheService.set("categories:hot", result, 300, TimeUnit.SECONDS);
        return result;
    }

    /**
     * 清除所有分类相关的缓存
     * 在分类 CRUD 操作后调用，确保数据一致性
     */
    public void clearCategoryCache() {
        cacheService.delete("categories:all");
        cacheService.delete("categories:tree");
        cacheService.delete("categories:hot");
        log.info("分类缓存已清除");
    }

    private String getParentName(Category c) {
        if (c.getParentId() == null) return "";
        return categoryRepository.findById(c.getParentId())
                .map(Category::getName)
                .orElse("");
    }

    public List<Map<String, Object>> getBreadcrumb(Long id) {
        List<Map<String, Object>> breadcrumb = new ArrayList<>();
        Category current = categoryRepository.findById(id).orElse(null);
        while (current != null) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", current.getId());
            node.put("name", current.getName());
            node.put("level", current.getLevel());
            breadcrumb.add(0, node);
            current = current.getParentId() != null
                    ? categoryRepository.findById(current.getParentId()).orElse(null)
                    : null;
        }
        return breadcrumb;
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

    public Page<CategoryChangeLog> getCategoryChangeLogs(Long categoryId, Pageable pageable) {
        if (categoryId != null) {
            return categoryChangeLogRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
        }
        return categoryChangeLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public String exportCategories() {
        List<Category> categories = categoryRepository.findAll();
        StringBuilder sb = new StringBuilder();
        // 中文列名
        sb.append("分类ID,分类名称,上级分类ID,层级,排序,状态,图标,描述,关键词\n");
        for (Category c : categories) {
            sb.append(c.getId()).append(",")
              .append(escapeCsv(c.getName())).append(",")
              .append(c.getParentId() != null ? c.getParentId() : "无").append(",")
              .append(formatLevel(c.getLevel())).append(",")
              .append(c.getSort() != null ? c.getSort() : 0).append(",")
              .append(c.getStatus() != null && c.getStatus() ? "启用" : "禁用").append(",")
              .append(escapeCsv(c.getIcon())).append(",")
              .append(escapeCsv(c.getDescription())).append(",")
              .append(escapeCsv(c.getKeywords())).append("\n");
        }
        return sb.toString();
    }

    /**
     * 格式化层级显示
     * @param level 层级值
     * @return 中文层级描述
     */
    private String formatLevel(Integer level) {
        if (level == null) {
            return "一级分类";
        }
        switch (level) {
            case 1: return "一级分类";
            case 2: return "二级分类";
            case 3: return "三级分类";
            default: return "第" + level + "级分类";
        }
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

    private record CategoryData(List<Category> categories, Map<Long, List<Long>> childrenMap, Map<Long, Long> directCountMap) {}

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

    private void collectAllChildCategoryIds(Long categoryId, Map<Long, List<Long>> categoryChildrenMap, List<Long> result) {
        List<Long> children = categoryChildrenMap.get(categoryId);
        if (children != null) {
            for (Long childId : children) {
                result.add(childId);
                collectAllChildCategoryIds(childId, categoryChildrenMap, result);
            }
        }
    }

    private Long calculateItemCount(Long categoryId, Map<Long, List<Long>> categoryChildrenMap) {
        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(categoryId);
        collectAllChildCategoryIds(categoryId, categoryChildrenMap, categoryIds);
        return itemRepository.countByCategoryIds(categoryIds);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
