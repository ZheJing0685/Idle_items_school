package com.idleitems.school.module.item.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.module.item.dto.ItemSummaryDTO;
import com.idleitems.school.module.item.dto.RelatedItemDTO;
import com.idleitems.school.module.category.entity.Category;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.category.repository.CategoryRepository;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.order.repository.OrderRepository;
import com.idleitems.school.module.user.repository.UserRepository;
import com.idleitems.school.shared.cache.CacheService;
import com.idleitems.school.util.ItemDTOConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemQueryService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final CacheService cacheService;
    private final UserRepository userRepository;
    private final ViewCountService viewCountService;
    private final OrderRepository orderRepository;
    private final ItemDTOConverter dtoConverter;

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

        for (Long userId : userIds) {
            String cacheKey = CacheService.getSellerItemCountKey(userId);
            Object cached = cacheService.get(cacheKey);
            if (cached instanceof Integer) {
                result.put(userId, (Integer) cached);
            } else {
                uncachedUserIds.add(userId);
            }
        }

        if (!uncachedUserIds.isEmpty()) {
            List<Object[]> counts = itemRepository.countByUserIds(uncachedUserIds);
            for (Object[] count : counts) {
                Long userId = (Long) count[0];
                int intCount = ((Long) count[1]).intValue();
                result.put(userId, intCount);
                cacheService.set(CacheService.getSellerItemCountKey(userId), intCount, 3600, TimeUnit.SECONDS);
            }
        }

        for (Long userId : userIds) {
            result.putIfAbsent(userId, 0);
        }
        return result;
    }

    public Page<ItemSummaryDTO> getItems(int page, int size, String categoryIdStr, String sortBy, String condition, String deliveryMethod, String keyword) {
        Long categoryId = parseCategoryId(categoryIdStr);

        String cacheKey = CacheService.getItemListKey(page, size,
                categoryId != null ? categoryId.toString() : "all", sortBy, condition,
                deliveryMethod != null && !deliveryMethod.isEmpty() ? Integer.valueOf(deliveryMethod) : null,
                keyword);

        Object cachedObject = cacheService.get(cacheKey);
        if (cachedObject instanceof Page) {
            @SuppressWarnings("unchecked")
            Page<ItemSummaryDTO> cachedPage = (Page<ItemSummaryDTO>) cachedObject;
            return cachedPage;
        }

        Item.ItemCondition itemCondition = parseCondition(condition);
        Pageable pageable = createPageable(page, size, sortBy);
        Page<Item> itemsPage = queryItems(categoryId, itemCondition, deliveryMethod, keyword, pageable);

        Page<ItemSummaryDTO> result = convertToSummaryPage(itemsPage);
        cacheService.set(cacheKey, result, 300, TimeUnit.SECONDS);
        return result;
    }

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

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Item> recentItems = itemRepository.findByStatusAndCreatedAtBetween(
                Item.ItemStatus.ON_SALE, thirtyDaysAgo, LocalDateTime.now());

        Map<Long, Long> categoryItemCounts = computeCategoryItemCounts(recentItems);
        long maxCategoryCount = categoryItemCounts.values().stream().mapToLong(Long::longValue).max().orElse(1);

        double now = System.currentTimeMillis();
        List<Item> sorted = recentItems.stream()
                .sorted((a, b) -> Double.compare(calculateHotScore(b, now, categoryItemCounts, maxCategoryCount),
                                                  calculateHotScore(a, now, categoryItemCounts, maxCategoryCount)))
                .limit(10)
                .toList();

        List<ItemSummaryDTO> result = convertToSummaryDTOListWithRatings(sorted);
        cacheService.set(cacheKey, result, 600, TimeUnit.SECONDS);
        return result;
    }

    public Map<String, Object> getRelatedItems(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));

        Long categoryId = item.getCategoryId();
        BigDecimal price = item.getPrice();
        BigDecimal minPrice = price.multiply(BigDecimal.valueOf(0.7));
        BigDecimal maxPrice = price.multiply(BigDecimal.valueOf(1.3));

        List<RelatedItemDTO> similarItems = itemRepository
                .findRelatedByCategoryAndPriceRange(categoryId, itemId, minPrice, maxPrice,
                        PageRequest.of(0, 6))
                .stream()
                .map(this::toRelatedItemDTO)
                .toList();

        List<RelatedItemDTO> sellerItems = itemRepository
                .findOtherItemsBySeller(item.getUserId(), itemId,
                        PageRequest.of(0, 4))
                .stream()
                .map(this::toRelatedItemDTO)
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("similarItems", similarItems);
        result.put("sellerItems", sellerItems);
        return result;
    }

    private RelatedItemDTO toRelatedItemDTO(Item item) {
        enrichItemWithSellerInfo(item);
        RelatedItemDTO dto = new RelatedItemDTO();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setPrice(item.getPrice());
        dto.setCoverImage(item.getCoverImage());
        dto.setCondition(item.getCondition().name());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setSellerNickname(item.getSellerNickname() != null ? item.getSellerNickname() : "未知卖家");
        return dto;
    }

    public Page<ItemSummaryDTO> searchItems(String keyword, int page, int size, String sortBy) {
        Pageable pageable = createPageable(page, size, sortBy);
        Page<Item> itemsPage = itemRepository.searchByKeyword(keyword, Item.ItemStatus.ON_SALE, pageable);
        return convertToSummaryPage(itemsPage);
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
        enrichItemWithSellerInfo(item);

        cacheService.set(cacheKey, item, 600, TimeUnit.SECONDS);
        return item;
    }

    public Page<Item> getUserItems(Long userId, Item.ItemStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status != null) {
            return itemRepository.findByUserIdAndStatus(userId, status, pageable);
        }
        return itemRepository.findByUserId(userId, pageable);
    }

    public Page<ItemSummaryDTO> getUserItemsAsSummary(Long userId, Item.ItemStatus status, int page, int size) {
        return convertToSummaryPage(getUserItems(userId, status, page, size));
    }

    public Pageable createPageable(int page, int size, String sortBy) {
        Sort sort;
        switch (sortBy) {
            case "priceAsc": sort = Sort.by(Sort.Direction.ASC, "price"); break;
            case "priceDesc": sort = Sort.by(Sort.Direction.DESC, "price"); break;
            case "viewCount": sort = Sort.by(Sort.Direction.DESC, "viewCount"); break;
            case "favoriteCount": sort = Sort.by(Sort.Direction.DESC, "favoriteCount"); break;
            case "createdAt": default: sort = Sort.by(Sort.Direction.DESC, "createdAt"); break;
        }
        return PageRequest.of(page - 1, size, sort);
    }

    private Long parseCategoryId(String categoryIdStr) {
        if (categoryIdStr == null || categoryIdStr.isEmpty()) return null;
        try {
            return Long.parseLong(categoryIdStr);
        } catch (NumberFormatException e) {
            log.warn("Invalid categoryId format: {}", categoryIdStr);
            return null;
        }
    }

    private Item.ItemCondition parseCondition(String condition) {
        if (condition == null || condition.isEmpty()) return null;
        try {
            return Item.ItemCondition.valueOf(condition);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Page<Item> queryItems(Long categoryId, Item.ItemCondition itemCondition, String deliveryMethod, String keyword, Pageable pageable) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category != null && category.getParentId() == null) {
                List<Category> subCategories = categoryRepository.findByParentId(categoryId);
                List<Long> categoryIds = new ArrayList<>();
                categoryIds.add(categoryId);
                subCategories.forEach(sub -> categoryIds.add(sub.getId()));
                return itemRepository.findByCategoryIdsAndFilters(Item.ItemStatus.ON_SALE, categoryIds, itemCondition, deliveryMethod, keyword, pageable);
            }
            return itemRepository.findByFilters(Item.ItemStatus.ON_SALE, categoryId, itemCondition, deliveryMethod, keyword, pageable);
        }
        return itemRepository.findByFilters(Item.ItemStatus.ON_SALE, null, itemCondition, deliveryMethod, keyword, pageable);
    }

    public Page<ItemSummaryDTO> convertToSummaryPage(Page<Item> itemsPage) {
        Set<Long> userIds = itemsPage.getContent().stream().map(Item::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = loadUserMap(userIds);
        Map<Long, Integer> sellerItemCounts = getSellerItemCounts(new ArrayList<>(userIds));

        List<ItemSummaryDTO> dtoList = dtoConverter.toSummaryDTOList(itemsPage.getContent(), userMap, sellerItemCounts);
        return new PageImpl<>(dtoList, itemsPage.getPageable(), itemsPage.getTotalElements());
    }

    private List<ItemSummaryDTO> convertToSummaryDTOListWithRatings(List<Item> items) {
        Set<Long> userIds = items.stream().map(Item::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = loadUserMap(userIds);
        Map<Long, Integer> sellerItemCounts = userIds.isEmpty() ? Map.of() :
                itemRepository.countByUserIds(List.copyOf(userIds)).stream()
                        .collect(Collectors.toMap(row -> (Long) row[0], row -> ((Number) row[1]).intValue()));
        return dtoConverter.toSummaryDTOList(items, userMap, sellerItemCounts);
    }

    private Map<Long, User> loadUserMap(Set<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
    }

    private void enrichItemWithSellerInfo(Item item) {
        int sellerItemCount = getSellerItemCount(item.getUserId());
        Optional<User> userOpt = userRepository.findById(item.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            item.setSellerNickname(user.getNickname() != null && !user.getNickname().isEmpty()
                    ? user.getNickname() : user.getUsername());
            item.setSellerVerified(user.getVerified() != null ? user.getVerified() : false);
            item.setSellerItemsCount(sellerItemCount);
            java.math.BigDecimal averageRating = dtoConverter.getAverageRating(item.getUserId());
            item.setSellerRating(averageRating != null ? averageRating.doubleValue() : 0.0);
        }
    }

    private double calculateHotScore(Item item, double nowMs, Map<Long, Long> categoryItemCounts, long maxCategoryCount) {
        long createdMillis = item.getCreatedAt() != null
                ? item.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                : 0L;
        double daysSinceCreation = (nowMs - createdMillis) / (1000.0 * 60 * 60 * 24);
        double timeDecay = 1.0 / (1.0 + daysSinceCreation / 14.0);

        int viewCount = item.getViewCount() != null ? item.getViewCount() : 0;
        int favoriteCount = item.getFavoriteCount() != null ? item.getFavoriteCount() : 0;
        double qualityFactor = viewCount > 0 ? (double) favoriteCount / (viewCount + 1) : 0;

        Long orderCount = orderRepository.countCompletedByItemId(item.getId());

        double categoryPenalty = 1.0;
        if (maxCategoryCount > 0) {
            long catCount = categoryItemCounts.getOrDefault(item.getCategoryId(), 0L);
            categoryPenalty = 1.0 - 0.3 * ((double) catCount / maxCategoryCount);
        }

        return (viewCount * 0.3 * timeDecay * (1 + qualityFactor * 20))
                + (favoriteCount * 5.0 * timeDecay)
                + (orderCount * 15.0 * timeDecay)
                * categoryPenalty;
    }

    private Map<Long, Long> computeCategoryItemCounts(List<Item> items) {
        return items.stream()
                .collect(Collectors.groupingBy(Item::getCategoryId, Collectors.counting()));
    }
}
