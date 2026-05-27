package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.dto.CreateItemRequest;
import com.idleitems.school.dto.ItemSummaryDTO;
import com.idleitems.school.dto.UpdateItemRequest;
import com.idleitems.school.entity.Category;
import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.ItemImage;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.CategoryRepository;
import com.idleitems.school.repository.ItemImageRepository;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.ReviewRepository;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.cache.CacheService;
import com.idleitems.school.util.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final CacheService cacheService;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ViewCountService viewCountService;
    private final ItemImageRepository itemImageRepository;

    public int getSellerItemCount(Long userId) {
        String cacheKey = CacheService.getSellerItemCountKey(userId);

        Object cached = cacheService.get(cacheKey);
        if (cached instanceof Integer) {
            return (Integer) cached;
        }

        int count = itemRepository.countByUserId(userId).intValue();
        cacheService.set(cacheKey, count, 3600, TimeUnit.SECONDS);

        return count;
    }

    public Map<Long, Integer> getSellerItemCounts(List<Long> userIds) {
        Map<Long, Integer> result = new HashMap<>();
        List<Long> uncachedUserIds = new ArrayList<>();

        // 先从缓存中获取
        for (Long userId : userIds) {
            String cacheKey = CacheService.getSellerItemCountKey(userId);
            Object cached = cacheService.get(cacheKey);
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
                String cacheKey = CacheService.getSellerItemCountKey(userId);
                cacheService.set(cacheKey, intCount, 3600, TimeUnit.SECONDS);
            }
        }

        // 为没有物品的用户设置默认值0
        for (Long userId : userIds) {
            result.putIfAbsent(userId, 0);
        }

        return result;
    }

    @Transactional
    public Item createItem(Long userId, CreateItemRequest req) {
        // 敏感词检查
        checkSensitiveWords(req.getTitle(), req.getDescription());

        Item item = new Item();
        item.setUserId(userId);
        item.setStatus(Item.ItemStatus.PENDING);
        item.setTitle(req.getTitle());
        item.setDescription(req.getDescription());
        item.setPrice(req.getPrice());
        item.setOriginalPrice(req.getOriginalPrice());
        item.setMinPrice(req.getMinPrice());
        item.setCategoryId(req.getCategoryId());
        item.setCondition(req.getCondition() != null ? Item.ItemCondition.valueOf(req.getCondition()) : Item.ItemCondition.GOOD);
        item.setDeliveryMethod(req.getDeliveryMethod());
        item.setContactType(req.getContactType());
        item.setIsBargainAllowed(req.getIsBargainAllowed() != null ? req.getIsBargainAllowed() : true);
        item.setLocation(req.getLocation());
        item.setBrand(req.getBrand());
        item.setWarrantyInfo(req.getWarrantyInfo());
        item.setTags(req.getTags());
        item.setContactName(req.getContactName());
        item.setContactPhone(req.getContactPhone());
        item.setContactInfo(req.getContactInfo());

        List<String> images = req.getImages();
        if (images != null && !images.isEmpty()) {
            item.setCoverImage(req.getCoverImage() != null ? req.getCoverImage() : images.get(0));
        }

        Item savedItem = itemRepository.save(item);

        // 持久化图片到item_images表
        if (images != null && !images.isEmpty()) {
            saveItemImages(savedItem.getId(), images, req.getCoverImage());
        }

        cacheService.deletePattern("item:list:*");
        cacheService.deletePattern("item:hot");
        cacheService.delete("categories:all");
        cacheService.delete("categories:tree");

        return savedItem;
    }

    /**
     * 保存物品图片到item_images表
     */
    private void saveItemImages(Long itemId, List<String> imageUrls, String coverImage) {
        String cover = coverImage != null ? coverImage : (imageUrls.isEmpty() ? null : imageUrls.get(0));
        List<ItemImage> itemImages = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            ItemImage itemImage = new ItemImage();
            itemImage.setItemId(itemId);
            itemImage.setImageUrl(imageUrls.get(i));
            itemImage.setIsCover(imageUrls.get(i).equals(cover));
            itemImage.setSortOrder(i);
            itemImages.add(itemImage);
        }
        itemImageRepository.saveAll(itemImages);
    }

    public Page<ItemSummaryDTO> getItems(int page, int size, String categoryIdStr, String sortBy, String condition, String deliveryMethod) {
        Long categoryId = null;
        if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
            try {
                categoryId = Long.parseLong(categoryIdStr);
            } catch (NumberFormatException e) {
                log.warn("Invalid categoryId format: {}", categoryIdStr);
            }
        }
        
        String cacheKey = CacheService.getItemListKey(page, size, categoryId != null ? categoryId.toString() : "all", sortBy, condition,
                deliveryMethod != null && !deliveryMethod.isEmpty() ? Integer.valueOf(deliveryMethod) : null);
        
        Object cachedObject = cacheService.get(cacheKey);
        if (cachedObject != null && cachedObject instanceof Page) {
            @SuppressWarnings("unchecked")
            Page<ItemSummaryDTO> cachedPage = (Page<ItemSummaryDTO>) cachedObject;
            return cachedPage;
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
                // 父分类：查询所有子分类ID，使用数据库查询替代Java过滤
                List<Category> subCategories = categoryRepository.findByParentId(categoryId);
                List<Long> categoryIds = new ArrayList<>();
                categoryIds.add(categoryId);
                subCategories.forEach(sub -> categoryIds.add(sub.getId()));

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
        
        cacheService.set(cacheKey, dtoPage, 300, TimeUnit.SECONDS);
        
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
        
        // 从评价表计算卖家真实评分
        BigDecimal averageRating = reviewRepository.getAverageRatingByUserId(item.getUserId());
        dto.setSellerRating(averageRating != null ? averageRating.doubleValue() : 0.0);
        
        return dto;
    }

    /**
     * 获取热门物品，使用浏览量 + 时间衰减的综合排序算法
     * 热度 = 浏览量 × 时间衰减系数
     * 时间衰减系数 = 1 / (1 + 天数/7)，7天前的物品热度衰减约50%
     */
    public List<ItemSummaryDTO> getHotItems() {
        String cacheKey = CacheService.getHotItemsKey();

        Object cachedObject = cacheService.get(cacheKey);
        if (cachedObject instanceof List) {
            @SuppressWarnings("unchecked")
            List<ItemSummaryDTO> cached = (List<ItemSummaryDTO>) cachedObject;
            if (!cached.isEmpty()) {
                return cached;
            }
        }

        // 查询最近30天内上架的物品
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Item> recentItems = itemRepository.findByStatusAndCreatedAtBetween(
                Item.ItemStatus.ON_SALE, thirtyDaysAgo, LocalDateTime.now());

        // 计算热度分数并排序
        double now = System.currentTimeMillis();
        List<Item> sorted = recentItems.stream()
                .sorted((a, b) -> {
                    double scoreA = calculateHotScore(a, now);
                    double scoreB = calculateHotScore(b, now);
                    return Double.compare(scoreB, scoreA);
                })
                .limit(10)
                .toList();

        // 批量填充卖家信息
        Set<Long> userIds = sorted.stream().map(Item::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userRepository.findAllById(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Integer> sellerItemCounts = userIds.isEmpty() ? Map.of() :
                itemRepository.countByUserIds(List.copyOf(userIds)).stream()
                        .collect(Collectors.toMap(row -> (Long) row[0], row -> ((Number) row[1]).intValue()));
        Map<Long, Double> sellerRatings = new HashMap<>();
        for (Long userId : userIds) {
            BigDecimal avg = reviewRepository.getAverageRatingByUserId(userId);
            sellerRatings.put(userId, avg != null ? avg.doubleValue() : 0.0);
        }

        List<ItemSummaryDTO> result = sorted.stream().map(item -> {
            ItemSummaryDTO dto = convertToDTO(item, userMap, sellerItemCounts);
            User user = userMap.get(item.getUserId());
            if (user != null) {
                dto.setSellerRating(sellerRatings.getOrDefault(item.getUserId(), 0.0));
            }
            return dto;
        }).toList();

        cacheService.set(cacheKey, result, 600, TimeUnit.SECONDS);
        return result;
    }

    /**
     * 计算物品热度分数
     * 热度 = 浏览量权重 × 时间衰减 + 收藏量权重
     */
    private double calculateHotScore(Item item, double nowMs) {
        long createdMillis = item.getCreatedAt() != null
                ? item.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                : 0L;
        double daysSinceCreation = (nowMs - createdMillis) / (1000.0 * 60 * 60 * 24);
        double timeDecay = 1.0 / (1.0 + daysSinceCreation / 7.0);

        int viewCount = item.getViewCount() != null ? item.getViewCount() : 0;
        int favoriteCount = item.getFavoriteCount() != null ? item.getFavoriteCount() : 0;

        return viewCount * timeDecay + favoriteCount * 5.0;
    }
    
    public Page<Item> getUserItems(Long userId, Item.ItemStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status != null) {
            return itemRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            return itemRepository.findByUserId(userId, pageable);
        }
    }

    public Page<ItemSummaryDTO> searchItems(String keyword, int page, int size, String sortBy) {
        Pageable pageable = createPageable(page, size, sortBy);
        Page<Item> itemsPage = itemRepository.searchByKeyword(keyword, Item.ItemStatus.ON_SALE, pageable);

        // 批量获取用户信息
        Set<Long> userIds = itemsPage.getContent().stream()
                .map(Item::getUserId)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userRepository.findAllById(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        }

        Map<Long, Integer> sellerItemCounts = getSellerItemCounts(new ArrayList<>(userIds));

        final Map<Long, User> finalUserMap = userMap;
        List<ItemSummaryDTO> dtoList = itemsPage.getContent().stream()
                .map(item -> convertToDTO(item, finalUserMap, sellerItemCounts))
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, itemsPage.getPageable(), itemsPage.getTotalElements());
    }

    public Item getItemById(Long id) {
        String cacheKey = CacheService.getItemKey(id);
        
        Object cachedObject = cacheService.get(cacheKey);
        if (cachedObject instanceof Item) {
            viewCountService.increment(id);
            return (Item) cachedObject;
        }
        
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));
        
        viewCountService.increment(id);
        
        // 直接在Service中填充卖家信息，避免Controller中的N+1
        int sellerItemCount = getSellerItemCount(item.getUserId());
        Optional<User> userOpt = userRepository.findById(item.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            item.setSellerNickname(
                user.getNickname() != null && !user.getNickname().isEmpty() 
                    ? user.getNickname() 
                    : user.getUsername()
            );
            item.setSellerVerified(user.getVerified() != null ? user.getVerified() : false);
            item.setSellerItemsCount(sellerItemCount);
            
            // 从评价表计算卖家真实评分
            BigDecimal averageRating = reviewRepository.getAverageRatingByUserId(item.getUserId());
            item.setSellerRating(averageRating != null ? averageRating.doubleValue() : 0.0);
        }
        
        cacheService.set(cacheKey, item, 600, TimeUnit.SECONDS);
        
        return item;
    }

    @Transactional
    public Item updateItem(Long userId, Long itemId, UpdateItemRequest req) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));

        if (!existingItem.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权修改此物品");
        }

        // 敏感词检查
        String title = req.getTitle() != null ? req.getTitle() : existingItem.getTitle();
        String description = req.getDescription() != null ? req.getDescription() : existingItem.getDescription();
        checkSensitiveWords(title, description);

        if (req.getTitle() != null) existingItem.setTitle(req.getTitle());
        if (req.getDescription() != null) existingItem.setDescription(req.getDescription());
        if (req.getPrice() != null) existingItem.setPrice(req.getPrice());
        if (req.getOriginalPrice() != null) existingItem.setOriginalPrice(req.getOriginalPrice());
        if (req.getMinPrice() != null) existingItem.setMinPrice(req.getMinPrice());
        if (req.getCategoryId() != null) existingItem.setCategoryId(req.getCategoryId());
        if (req.getCondition() != null) existingItem.setCondition(Item.ItemCondition.valueOf(req.getCondition()));
        if (req.getDeliveryMethod() != null) existingItem.setDeliveryMethod(req.getDeliveryMethod());
        if (req.getContactType() != null) existingItem.setContactType(req.getContactType());
        if (req.getIsBargainAllowed() != null) existingItem.setIsBargainAllowed(req.getIsBargainAllowed());
        if (req.getLocation() != null) existingItem.setLocation(req.getLocation());
        if (req.getBrand() != null) existingItem.setBrand(req.getBrand());
        if (req.getWarrantyInfo() != null) existingItem.setWarrantyInfo(req.getWarrantyInfo());
        if (req.getTags() != null) existingItem.setTags(req.getTags());
        if (req.getContactName() != null) existingItem.setContactName(req.getContactName());
        if (req.getContactPhone() != null) existingItem.setContactPhone(req.getContactPhone());
        if (req.getContactInfo() != null) existingItem.setContactInfo(req.getContactInfo());

        List<String> images = req.getImages();
        if (images != null && !images.isEmpty()) {
            existingItem.setCoverImage(req.getCoverImage() != null ? req.getCoverImage() : images.get(0));
        }

        existingItem.setStatus(Item.ItemStatus.PENDING);

        Item updatedItem = itemRepository.save(existingItem);

        cacheService.delete(CacheService.getItemKey(itemId));
        cacheService.deletePattern("item:list:*");
        cacheService.deletePattern("item:hot");

        return updatedItem;
    }

    @Transactional
    public Item offShelfItem(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));

        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作此物品");
        }

        item.setStatus(Item.ItemStatus.OFF_SHELF);
        Item updatedItem = itemRepository.save(item);

        cacheService.delete(CacheService.getItemKey(itemId));
        cacheService.deletePattern("item:list:*");
        cacheService.deletePattern("item:hot");

        return updatedItem;
    }

    @Transactional
    public Item onShelfItem(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));

        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作此物品");
        }

        if (item.getStatus() != Item.ItemStatus.OFF_SHELF && item.getStatus() != Item.ItemStatus.DRAFT) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "只有下架或草稿状态的物品才能上架");
        }

        item.setStatus(Item.ItemStatus.ON_SALE);
        Item updatedItem = itemRepository.save(item);

        cacheService.delete(CacheService.getItemKey(itemId));
        cacheService.deletePattern("item:list:*");
        cacheService.deletePattern("item:hot");

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
            case "favoriteCount":
                sort = Sort.by(Sort.Direction.DESC, "favoriteCount");
                break;
            case "createdAt":
            default:
                sort = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
        }
        return PageRequest.of(page - 1, size, sort);
    }

    public List<Item> getItemsForExport(String keyword, Item.ItemStatus status, Long categoryId) {
        Pageable pageable = Pageable.unpaged();
        if (status != null) {
            return itemRepository.findByStatus(status, pageable).getContent();
        } else {
            return itemRepository.findAll(pageable).getContent();
        }
    }

    public long countItems() {
        return itemRepository.count();
    }

    public long countItemsByStatus(Item.ItemStatus status) {
        return itemRepository.countByStatus(status);
    }

    public Page<Item> getAdminItems(Pageable pageable, Item.ItemStatus status) {
        if (status != null) {
            return itemRepository.findByStatus(status, pageable);
        }
        return itemRepository.findAll(pageable);
    }

    @Transactional
    public Item approveItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
        item.setStatus(Item.ItemStatus.ON_SALE);
        Item savedItem = itemRepository.save(item);
        clearItemCache(id);
        return savedItem;
    }

    @Transactional
    public Item rejectItem(Long id, String reason) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
        item.setStatus(Item.ItemStatus.REJECTED);
        item.setRejectReason(reason);
        Item savedItem = itemRepository.save(item);
        clearItemCache(id);
        return savedItem;
    }

    @Transactional
    public Item forceOffShelfItem(Long id, String reason) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
        item.setStatus(Item.ItemStatus.OFF_SHELF);
        if (reason != null) {
            item.setRejectReason(reason);
        }
        Item savedItem = itemRepository.save(item);
        clearItemCache(id);
        return savedItem;
    }

    public boolean existsOrderByItemId(Long itemId) {
        return orderRepository.existsByItemId(itemId);
    }

    @Transactional
    public void deleteItemById(Long id) {
        itemRepository.deleteById(id);
        clearItemCache(id);
    }

    @Transactional
    public void deleteItemByUser(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "物品不存在"));

        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权删除该物品");
        }

        if (item.getStatus() == Item.ItemStatus.SOLD) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "已售出的物品无法删除");
        }

        itemRepository.delete(item);
        clearItemCache(itemId);
    }

    private void clearItemCache(Long itemId) {
        cacheService.delete(CacheService.getItemKey(itemId));
        cacheService.deletePattern("item:list:*");
        cacheService.deletePattern("item:hot");
    }

    /**
     * 敏感词检查
     */
    private void checkSensitiveWords(String title, String description) {
        java.util.List<String> words = new java.util.ArrayList<>();
        words.addAll(SensitiveWordFilter.findSensitiveWords(title));
        words.addAll(SensitiveWordFilter.findSensitiveWords(description));
        if (!words.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    SensitiveWordFilter.getWarningMessage(words));
        }
    }
}
