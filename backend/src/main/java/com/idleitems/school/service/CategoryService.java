package com.idleitems.school.service;

import com.idleitems.school.entity.Category;
import com.idleitems.school.repository.CategoryRepository;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.util.CacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final CacheManager cacheManager;
    
    private static final String CATEGORIES_CACHE_KEY = "categories:all";
    private static final String CATEGORY_TREE_CACHE_KEY = "categories:tree";

    public List<Map<String, Object>> getAllCategories() {
        // 检查缓存
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
        
        // 缓存结果30分钟
        try {
            cacheManager.set(CATEGORIES_CACHE_KEY, result, 1800);
        } catch (Exception e) {
            log.error("设置分类缓存失败: {}", e.getMessage());
        }
        return result;
    }

    public List<Map<String, Object>> getCategoryTree() {
        // 检查缓存
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

        // 缓存结果30分钟
        try {
            cacheManager.set(CATEGORY_TREE_CACHE_KEY, tree, 1800);
        } catch (Exception e) {
            log.error("设置分类树缓存失败: {}", e.getMessage());
        }
        return tree;
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
}
