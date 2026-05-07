package com.idleitems.school.service;

import com.idleitems.school.dto.ItemSummaryDTO;
import com.idleitems.school.entity.Category;
import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.CategoryRepository;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.util.CacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final CacheManager cacheManager;
    private final UserRepository userRepository;

    @Async("viewCountExecutor")
    @Transactional
    public void incrementViewCountAsync(Long itemId) {
        try {
            Optional<Item> optionalItem = itemRepository.findById(itemId);
            if (optionalItem.isPresent()) {
                Item item = optionalItem.get();
                item.setViewCount(item.getViewCount() + 1);
                itemRepository.save(item);
                cacheManager.delete(CacheManager.getHotItemsKey());
                log.debug("View count incremented for item: {}", itemId);
            }
        } catch (Exception e) {
            log.error("Failed to increment view count for item {}: {}", itemId, e.getMessage());
        }
    }

    public int getSellerItemCount(Long userId) {
        String cacheKey = CacheManager.getSellerItemCountKey(userId);

        Object cached = cacheManager.get(cacheKey);
        if (cached instanceof Integer) {
            return (Integer) cached;
        }

        int count = itemRepository.countByUserId(userId).intValue();
        cacheManager.set(cacheKey, count, 3600);

        return count;
    }

    public Map<Long, Integer> getSellerItemCounts(List<Long> userIds) {
        Map<Long, Integer> result = new HashMap<>();
        List<Long> uncachedUserIds = new ArrayList<>();

        // 先从缓存中获取
        for (Long userId : userIds) {
            String cacheKey = CacheManager.getSellerItemCountKey(userId);
            Object cached = cacheManager.get(cacheKey);
            if (cached instanceof Integer) {
                result.put(userId, (Integer) cached);
            } else {
                uncachedUserIds.add(userId);
            }
        }

        // 批量查询未缓存的用户物品数量
        if (!uncachedUserIds.isEmpty()) {
            List<Object[]> counts = itemRepository.countByUserIds(uncachedUserIds);
            for (Object[] count : counts) {
                Long userId = (Long) count[0];
                Long countValue = (Long) count[1];
                int intCount = countValue.intValue();
                result.put(userId, intCount);
                
                // 缓存结果
                String cacheKey = CacheManager.getSellerItemCountKey(userId);
                cacheManager.set(cacheKey, intCount, 3600);
            }
        }

        // 为没有物品的用户设置默认值0
        for (Long userId : userIds) {
            result.putIfAbsent(userId, 0);
        }

        return result;
    }

    @Transactional
    public Item createItem(Long userId, Map<String, Object> request) throws Exception {
        Item item = new Item();
        item.setUserId(userId);
        item.setStatus(Item.ItemStatus.PENDING);
        item.setTitle((String) request.get("title"));
        item.setDescription((String) request.get("description"));
        item.setPrice(request.get("price") != null ? new BigDecimal(request.get("price").toString()) : null);
        item.setOriginalPrice(request.get("originalPrice") != null ? new BigDecimal(request.get("originalPrice").toString()) : null);
        item.setMinPrice(request.get("minPrice") != null ? new BigDecimal(request.get("minPrice").toString()) : null);
        item.setCategoryId(request.get("categoryId") != null ? Long.valueOf(request.get("categoryId").toString()) : null);
        item.setCondition(request.get("condition") != null ? Item.ItemCondition.valueOf((String) request.get("condition")) : Item.ItemCondition.GOOD);
        item.setDeliveryMethod(request.get("deliveryMethod") != null ? Integer.valueOf(request.get("deliveryMethod").toString()) : null);
        item.setContactType(request.get("contactType") != null ? Integer.valueOf(request.get("contactType").toString()) : null);
        item.setIsBargainAllowed(request.get("isBargainAllowed") != null ? (Boolean) request.get("isBargainAllowed") : true);
        item.setLocation((String) request.get("location"));
        item.setBrand((String) request.get("brand"));
        item.setWarrantyInfo((String) request.get("warrantyInfo"));
        item.setTags((String) request.get("tags"));
        item.setContactName((String) request.get("contactName"));
        item.setContactPhone((String) request.get("contactPhone"));
        
        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) request.get("images");
        if (images != null && !images.isEmpty()) {
            if (item.getCoverImage() == null) {
                item.setCoverImage(images.get(0));
            }
        }
        
        Item savedItem = itemRepository.save(item);

        cacheManager.deletePattern("item:list:*");
        cacheManager.deletePattern("item:hot");
        cacheManager.delete("categories:all");
        cacheManager.delete("categories:tree");

        return savedItem;
    }

    public Page<ItemSummaryDTO> getItems(int page, int size, String categoryIdStr, String sortBy, String condition, Integer deliveryMethod) {
        Long categoryId = null;
        if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
            try {
                categoryId = Long.parseLong(categoryIdStr);
            } catch (NumberFormatException e) {
                log.warn("Invalid categoryId format: {}", categoryIdStr);
            }
        }
        
        String cacheKey = CacheManager.getItemListKey(page, size, categoryId != null ? categoryId.toString() : "all", sortBy, condition, deliveryMethod);
        
        Object cachedObject = cacheManager.get(cacheKey);
        if (cachedObject != null && cachedObject instanceof Page) {
            return (Page<ItemSummaryDTO>) cachedObject;
        }
        
        Item.ItemCondition itemCondition = null;
        if (condition != null && !condition.isEmpty()) {
            try {
                itemCondition = Item.ItemCondition.valueOf(condition);
            } catch (IllegalArgumentException e) {
                // 无效的 condition 值，忽略筛选条件
            }
        }
        
        Pageable pageable = createPageable(page, size, sortBy);
        Page<Item> itemsPage;
        
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category != null && category.getParentId() == null) {
                final Long finalCategoryId = categoryId;
                List<Category> subCategories = categoryRepository.findAll().stream()
                    .filter(c -> finalCategoryId.equals(c.getParentId()))
                    .toList();
                
                List<Long> categoryIds = new ArrayList<>();
                categoryIds.add(categoryId);
                for (Category sub : subCategories) {
                    categoryIds.add(sub.getId());
                }
                
                itemsPage = itemRepository.findByCategoryIdsAndFilters(
                    Item.ItemStatus.ON_SALE, 
                    categoryIds, 
                    itemCondition, 
                    deliveryMethod, 
                    pageable
                );
            } else {
                itemsPage = itemRepository.findByFilters(
                    Item.ItemStatus.ON_SALE, 
                    categoryId, 
                    itemCondition, 
                    deliveryMethod, 
                    pageable
                );
            }
        } else {
            itemsPage = itemRepository.findByFilters(
                Item.ItemStatus.ON_SALE, 
                null, 
                itemCondition, 
                deliveryMethod, 
                pageable
            );
        }
        
        // 批量获取用户信息，避免N+1查询
        Set<Long> userIds = itemsPage.getContent().stream()
            .map(Item::getUserId)
            .collect(Collectors.toSet());
        
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userRepository.findAllById(userIds);
            userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        }
        
        // 批量获取用户物品数量，避免N+1查询
        final Map<Long, Integer> sellerItemCounts = getSellerItemCounts(new ArrayList<>(userIds));
        
        final Map<Long, User> finalUserMap = userMap;
        final List<Item> items = itemsPage.getContent();
        
        // 转换为DTO
        List<ItemSummaryDTO> dtoList = items.stream()
            .map(item -> convertToDTO(item, finalUserMap, sellerItemCounts))
            .collect(Collectors.toList());
        
        Page<ItemSummaryDTO> dtoPage = new org.springframework.data.domain.PageImpl<>(
            dtoList,
            itemsPage.getPageable(),
            itemsPage.getTotalElements()
        );
        
        cacheManager.set(cacheKey, dtoPage, 300);
        
        return dtoPage;
    }
    
    private ItemSummaryDTO convertToDTO(Item item, Map<Long, User> userMap, Map<Long, Integer> sellerItemCounts) {
        ItemSummaryDTO dto = ItemSummaryDTO.builder()
            .id(item.getId())
            .title(item.getTitle())
            .price(item.getPrice())
            .originalPrice(item.getOriginalPrice())
            .coverImage(item.getCoverImage())
            .viewCount(item.getViewCount())
            .favoriteCount(item.getFavoriteCount())
            .createdAt(item.getCreatedAt())
            .isBargainAllowed(item.getIsBargainAllowed())
            .condition(item.getCondition() != null ? item.getCondition().name() : null)
            .build();
            
        // 填充卖家信息
        User user = userMap.get(item.getUserId());
        if (user != null) {
            dto.setSellerNickname(
                user.getNickname() != null && !user.getNickname().isEmpty() 
                    ? user.getNickname() 
                    : user.getUsername()
            );
            dto.setSellerVerified(user.getVerified() != null ? user.getVerified() : false);
        }
        dto.setSellerItemsCount(sellerItemCounts.getOrDefault(item.getUserId(), 0));
        dto.setSellerRating(5.0);
        
        return dto;
    }

    public List<Item> getHotItems() {
        String cacheKey = CacheManager.getHotItemsKey();
        
        Object cachedObject = cacheManager.get(cacheKey);
        
        if (cachedObject instanceof List) {
            List<?> cachedList = (List<?>) cachedObject;
            if (!cachedList.isEmpty() && cachedList.get(0) instanceof Item) {
                return (List<Item>) cachedObject;
            }
        }
        
        List<Item> items = itemRepository.findTop10ByStatusOrderByViewCountDesc(Item.ItemStatus.ON_SALE);
        cacheManager.set(cacheKey, items, 600);
        
        return items;
    }

    public Page<Item> getUserItems(Long userId, Item.ItemStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status != null) {
            return itemRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            return itemRepository.findByUserId(userId, pageable);
        }
    }

    public Page<Item> searchItems(String keyword, int page, int size, String sortBy) {
        Pageable pageable = createPageable(page, size, sortBy);
        return itemRepository.searchByKeyword(keyword, Item.ItemStatus.ON_SALE, pageable);
    }

    public Item getItemById(Long id) {
        String cacheKey = CacheManager.getItemKey(id);
        
        Object cachedObject = cacheManager.get(cacheKey);
        if (cachedObject instanceof Item) {
            incrementViewCountAsync(id);
            return (Item) cachedObject;
        }
        
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
        item.setViewCount(item.getViewCount() + 1);
        Item savedItem = itemRepository.save(item);
        
        // 直接在Service中填充卖家信息，避免Controller中的N+1
        int sellerItemCount = getSellerItemCount(savedItem.getUserId());
        Optional<User> userOpt = userRepository.findById(savedItem.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            savedItem.setSellerNickname(
                user.getNickname() != null && !user.getNickname().isEmpty() 
                    ? user.getNickname() 
                    : user.getUsername()
            );
            savedItem.setSellerVerified(user.getVerified() != null ? user.getVerified() : false);
            savedItem.setSellerItemsCount(sellerItemCount);
            savedItem.setSellerRating(5.0);
        }
        
        cacheManager.set(cacheKey, savedItem, 600);
        
        return savedItem;
    }

    @Transactional
    public Item updateItem(Long userId, Long itemId, Map<String, Object> request) throws Exception {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));

        if (!existingItem.getUserId().equals(userId)) {
            throw new SecurityException("无权修改此物品");
        }

        existingItem.setTitle((String) request.get("title"));
        existingItem.setDescription((String) request.get("description"));
        existingItem.setPrice(request.get("price") != null ? new BigDecimal(request.get("price").toString()) : null);
        existingItem.setOriginalPrice(request.get("originalPrice") != null ? new BigDecimal(request.get("originalPrice").toString()) : null);
        existingItem.setMinPrice(request.get("minPrice") != null ? new BigDecimal(request.get("minPrice").toString()) : null);
        existingItem.setCategoryId(request.get("categoryId") != null ? Long.valueOf(request.get("categoryId").toString()) : null);
        existingItem.setCondition(request.get("condition") != null ? Item.ItemCondition.valueOf((String) request.get("condition")) : Item.ItemCondition.GOOD);
        existingItem.setDeliveryMethod(request.get("deliveryMethod") != null ? Integer.valueOf(request.get("deliveryMethod").toString()) : null);
        existingItem.setContactType(request.get("contactType") != null ? Integer.valueOf(request.get("contactType").toString()) : null);
        existingItem.setIsBargainAllowed(request.get("isBargainAllowed") != null ? (Boolean) request.get("isBargainAllowed") : true);
        existingItem.setLocation((String) request.get("location"));
        existingItem.setBrand((String) request.get("brand"));
        existingItem.setWarrantyInfo((String) request.get("warrantyInfo"));
        existingItem.setTags((String) request.get("tags"));
        existingItem.setContactName((String) request.get("contactName"));
        existingItem.setContactPhone((String) request.get("contactPhone"));
        existingItem.setStatus(Item.ItemStatus.PENDING);

        List<String> images = (List<String>) request.get("images");
        if (images != null && !images.isEmpty()) {
            if (request.get("coverImage") != null) {
                existingItem.setCoverImage((String) request.get("coverImage"));
            } else {
                existingItem.setCoverImage(images.get(0));
            }
        }

        Item updatedItem = itemRepository.save(existingItem);

        cacheManager.delete(CacheManager.getItemKey(itemId));
        cacheManager.deletePattern("item:list:*");
        cacheManager.deletePattern("item:hot");

        return updatedItem;
    }

    @Transactional
    public Item offShelfItem(Long userId, Long itemId) {
        Item item = itemRepository.findById(Objects.requireNonNull(itemId))
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));

        if (!item.getUserId().equals(userId)) {
            throw new SecurityException("无权操作此物品");
        }

        item.setStatus(Item.ItemStatus.OFF_SHELF);
        Item updatedItem = itemRepository.save(item);
        
        cacheManager.delete(CacheManager.getItemKey(itemId));
        cacheManager.deletePattern("item:list:*");
        cacheManager.deletePattern("item:hot");
        
        return updatedItem;
    }

    public Pageable createPageable(int page, int size, String sortBy) {
        Sort sort;
        switch (sortBy) {
            case "priceAsc":
                sort = Sort.by(Sort.Direction.ASC, "price");
                break;
            case "priceDesc":
                sort = Sort.by(Sort.Direction.DESC, "price");
                break;
            case "viewCount":
                sort = Sort.by(Sort.Direction.DESC, "viewCount");
                break;
            case "createdAt":
            default:
                sort = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
        }
        return PageRequest.of(page - 1, size, sort);
    }
}
