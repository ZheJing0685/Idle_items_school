package com.idleitems.school.service;

import com.idleitems.school.entity.Category;
import com.idleitems.school.entity.CategoryChangeLog;
import com.idleitems.school.entity.CategoryFeedback;
import com.idleitems.school.repository.CategoryChangeLogRepository;
import com.idleitems.school.repository.CategoryFeedbackRepository;
import com.idleitems.school.repository.CategoryRepository;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.cache.CacheService;
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

        cacheService.set("categories:all", result, 1800, TimeUnit.SECONDS);
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

        cacheService.set("categories:tree", tree, 1800, TimeUnit.SECONDS);
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
