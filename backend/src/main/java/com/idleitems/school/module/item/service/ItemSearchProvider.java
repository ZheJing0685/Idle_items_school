package com.idleitems.school.module.item.service;

import com.idleitems.school.module.item.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * 物品搜索提供者接口。
 * <p>
 * 默认使用 SQL LIKE 实现，可替换为 Elasticsearch 等搜索引擎实现。
 * 通过 {@code @ConditionalOnMissingBean(ItemSearchProvider.class)} 机制，
 * 引入搜索引擎依赖并提供实现类即可自动替换。
 */
public interface ItemSearchProvider {

    /**
     * 搜索符合条件物品的 ID 列表
     *
     * @param keyword    搜索关键词（可为 null）
     * @param categoryId 分类 ID（可为 null）
     * @param filters    额外筛选条件（如 condition、deliveryMethod 等）
     * @param pageable   分页参数
     * @return 物品 ID 分页列表
     */
    Page<Long> searchItemIds(String keyword, Long categoryId,
                             Map<String, Object> filters, Pageable pageable);

    /**
     * 索引物品（默认 SQL 实现为空操作）
     */
    default void indexItem(Item item) {
        // 默认不做任何操作
    }

    /**
     * 从索引中移除（默认 SQL 实现为空操作）
     */
    default void removeFromIndex(Long itemId) {
        // 默认不做任何操作
    }
}
