package com.idleitems.school.module.item.service.impl;

import com.idleitems.school.module.category.entity.Category;
import com.idleitems.school.module.category.repository.CategoryRepository;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.item.service.ItemSearchProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 默认物品搜索提供者，基于 SQL LIKE 查询实现。
 * <p>
 * 当项目中不存在其他 {@link ItemSearchProvider} 实现时自动生效。
 */
@Component
@ConditionalOnMissingBean(ItemSearchProvider.class)
public class DefaultItemSearchProvider implements ItemSearchProvider {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    public DefaultItemSearchProvider(ItemRepository itemRepository,
                                     CategoryRepository categoryRepository) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Page<Long> searchItemIds(String keyword, Long categoryId,
                                    Map<String, Object> filters, Pageable pageable) {
        // 解析筛选条件
        Item.ItemCondition condition = null;
        String deliveryMethod = null;
        if (filters != null) {
            if (filters.get("condition") instanceof String) {
                try {
                    condition = Item.ItemCondition.valueOf((String) filters.get("condition"));
                } catch (IllegalArgumentException ignored) {
                    // 忽略无效条件值
                }
            }
            if (filters.get("deliveryMethod") instanceof String) {
                deliveryMethod = (String) filters.get("deliveryMethod");
            }
        }

        // 有关键词时使用 LIKE 搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            return searchByKeywordWithFilters(keyword, categoryId, condition, deliveryMethod, pageable);
        }

        // 无关键词时使用分类 + 条件筛选
        return searchByCategoryWithFilters(categoryId, condition, deliveryMethod, pageable);
    }

    /**
     * 按关键词 + 可选筛选条件搜索
     */
    private Page<Long> searchByKeywordWithFilters(String keyword, Long categoryId,
                                                   Item.ItemCondition condition,
                                                   String deliveryMethod,
                                                   Pageable pageable) {
        // 关键词搜索已在 ItemRepository 中实现
        Page<Item> items = itemRepository.searchByKeyword(keyword, Item.ItemStatus.ON_SALE, pageable);

        // 如果有额外的 categoryId / condition / deliveryMethod 筛选，需要进一步过滤
        if (categoryId != null || condition != null || deliveryMethod != null) {
            List<Long> filteredIds = new ArrayList<>();
            List<Long> categoryIds = resolveCategoryIds(categoryId);

            for (Item item : items.getContent()) {
                boolean matches = true;
                if (categoryIds != null && !categoryIds.contains(item.getCategoryId())) {
                    matches = false;
                }
                if (condition != null && !condition.equals(item.getCondition())) {
                    matches = false;
                }
                if (deliveryMethod != null && !deliveryMethod.equals(item.getDeliveryMethod())) {
                    matches = false;
                }
                if (matches) {
                    filteredIds.add(item.getId());
                }
            }
            // 因为内存分页不准确，这里转换为分页
            long total = filteredIds.size();
            int start = pageable.getPageNumber() * pageable.getPageSize();
            int end = Math.min(start + pageable.getPageSize(), filteredIds.size());
            List<Long> pageContent = start < filteredIds.size()
                    ? filteredIds.subList(start, end)
                    : List.of();
            return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, total);
        }

        return items.map(Item::getId);
    }

    /**
     * 按分类 + 筛选条件搜索
     */
    private Page<Long> searchByCategoryWithFilters(Long categoryId,
                                                    Item.ItemCondition condition,
                                                    String deliveryMethod,
                                                    Pageable pageable) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category != null && category.getParentId() == null) {
                // 父分类：查询其下所有子分类的物品
                List<Category> subCategories = categoryRepository.findByParentId(categoryId);
                List<Long> categoryIds = new ArrayList<>();
                categoryIds.add(categoryId);
                subCategories.forEach(sub -> categoryIds.add(sub.getId()));
                Page<Item> items = itemRepository.findByCategoryIdsAndFilters(
                        Item.ItemStatus.ON_SALE, categoryIds, condition, deliveryMethod, null, pageable);
                return items.map(Item::getId);
            }
            // 叶子分类直接查询
            Page<Item> items = itemRepository.findByFilters(
                    Item.ItemStatus.ON_SALE, categoryId, condition, deliveryMethod, null, pageable);
            return items.map(Item::getId);
        }
        // 无分类筛选
        Page<Item> items = itemRepository.findByFilters(
                Item.ItemStatus.ON_SALE, null, condition, deliveryMethod, null, pageable);
        return items.map(Item::getId);
    }

    /**
     * 解析分类 ID 列表：如果是父分类则展开为所有子分类 ID
     */
    private List<Long> resolveCategoryIds(Long categoryId) {
        if (categoryId == null) return null;
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) return List.of(categoryId);

        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);
        if (category.getParentId() == null) {
            // 父分类，包含子分类
            List<Category> children = categoryRepository.findByParentId(categoryId);
            children.forEach(c -> ids.add(c.getId()));
        }
        return ids;
    }

    @Override
    public void indexItem(Item item) {
        // SQL 搜索不需要索引维护
    }

    @Override
    public void removeFromIndex(Long itemId) {
        // SQL 搜索不需要索引维护
    }
}
