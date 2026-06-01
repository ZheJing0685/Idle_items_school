package com.idleitems.school.util;

import com.idleitems.school.module.item.dto.ItemSummaryDTO;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.order.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemDTOConverter {

    private final ReviewRepository reviewRepository;

    public ItemSummaryDTO toSummaryDTO(Item item, Map<Long, User> userMap, Map<Long, Integer> sellerItemCounts) {
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

        User user = userMap.get(item.getUserId());
        if (user != null) {
            dto.setSellerNickname(user.getNickname() != null && !user.getNickname().isEmpty()
                    ? user.getNickname() : user.getUsername());
            dto.setSellerVerified(user.getVerified() != null ? user.getVerified() : false);
        }
        dto.setSellerItemsCount(sellerItemCounts.getOrDefault(item.getUserId(), 0));
        BigDecimal averageRating = reviewRepository.getAverageRatingByUserId(item.getUserId());
        dto.setSellerRating(averageRating != null ? averageRating.doubleValue() : 0.0);

        return dto;
    }

    public List<ItemSummaryDTO> toSummaryDTOList(List<Item> items, Map<Long, User> userMap, Map<Long, Integer> sellerItemCounts) {
        return items.stream()
                .map(item -> toSummaryDTO(item, userMap, sellerItemCounts))
                .collect(Collectors.toList());
    }

    public BigDecimal getAverageRating(Long userId) {
        return reviewRepository.getAverageRatingByUserId(userId);
    }
}
