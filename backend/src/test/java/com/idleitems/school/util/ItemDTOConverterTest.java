package com.idleitems.school.util;

import com.idleitems.school.module.item.dto.ItemSummaryDTO;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.order.repository.ReviewRepository;
import com.idleitems.school.module.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemDTOConverterTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ItemDTOConverter converter;

    private Item testItem;
    private User testUser;
    private Map<Long, User> userMap;
    private Map<Long, Integer> sellerItemCounts;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setUserId(10L);
        testItem.setTitle("测试物品");
        testItem.setPrice(new BigDecimal("99.99"));
        testItem.setOriginalPrice(new BigDecimal("199.99"));
        testItem.setCoverImage("cover.jpg");
        testItem.setViewCount(100);
        testItem.setFavoriteCount(20);
        testItem.setCreatedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
        testItem.setIsBargainAllowed(true);
        testItem.setCondition(Item.ItemCondition.GOOD);

        testUser = new User();
        testUser.setId(10L);
        testUser.setNickname("卖家昵称");
        testUser.setUsername("seller_user");
        testUser.setVerified(true);

        userMap = new HashMap<>();
        userMap.put(10L, testUser);

        sellerItemCounts = new HashMap<>();
        sellerItemCounts.put(10L, 5);
    }

    @Test
    void toSummaryDTO_WithUser_ReturnsDTOWithSellerInfo() {
        when(reviewRepository.getAverageRatingByUserId(10L)).thenReturn(new BigDecimal("4.5"));

        ItemSummaryDTO dto = converter.toSummaryDTO(testItem, userMap, sellerItemCounts);

        assertEquals(1L, dto.getId());
        assertEquals("测试物品", dto.getTitle());
        assertEquals(new BigDecimal("99.99"), dto.getPrice());
        assertEquals(new BigDecimal("199.99"), dto.getOriginalPrice());
        assertEquals("cover.jpg", dto.getCoverImage());
        assertEquals(100, dto.getViewCount());
        assertEquals(20, dto.getFavoriteCount());
        assertEquals("卖家昵称", dto.getSellerNickname());
        assertTrue(dto.getSellerVerified());
        assertEquals(5, dto.getSellerItemsCount());
        assertEquals(4.5, dto.getSellerRating(), 0.001);
        assertEquals("GOOD", dto.getCondition());

        verify(reviewRepository, times(1)).getAverageRatingByUserId(10L);
    }

    @Test
    void toSummaryDTO_WithoutUser_UsesNullForSellerFields() {
        userMap.clear();
        when(reviewRepository.getAverageRatingByUserId(10L)).thenReturn(null);

        ItemSummaryDTO dto = converter.toSummaryDTO(testItem, userMap, sellerItemCounts);

        assertNull(dto.getSellerNickname());
        assertNull(dto.getSellerVerified());
        assertEquals(5, dto.getSellerItemsCount());
        assertEquals(0.0, dto.getSellerRating(), 0.001);

        verify(reviewRepository, times(1)).getAverageRatingByUserId(10L);
    }

    @Test
    void toSummaryDTO_WithoutUserInMap_UsesNullForSellerFields() {
        Map<Long, User> emptyMap = new HashMap<>();
        when(reviewRepository.getAverageRatingByUserId(10L)).thenReturn(null);

        ItemSummaryDTO dto = converter.toSummaryDTO(testItem, emptyMap, new HashMap<>());

        assertNull(dto.getSellerNickname());
        assertNull(dto.getSellerVerified());
        assertEquals(0, dto.getSellerItemsCount());
        assertEquals(0.0, dto.getSellerRating(), 0.001);
    }

    @Test
    void toSummaryDTOList_MultipleItems_ReturnsList() {
        Item item2 = new Item();
        item2.setId(2L);
        item2.setUserId(20L);
        item2.setTitle("物品2");
        item2.setPrice(new BigDecimal("50.00"));
        item2.setCondition(Item.ItemCondition.LIKE_NEW);

        Map<Long, Integer> counts = new HashMap<>();
        counts.put(10L, 5);
        counts.put(20L, 3);

        when(reviewRepository.getAverageRatingByUserId(10L)).thenReturn(new BigDecimal("4.0"));
        when(reviewRepository.getAverageRatingByUserId(20L)).thenReturn(new BigDecimal("3.5"));

        List<ItemSummaryDTO> dtos = converter.toSummaryDTOList(List.of(testItem, item2), userMap, counts);

        assertEquals(2, dtos.size());
        assertEquals("测试物品", dtos.get(0).getTitle());
        assertEquals("物品2", dtos.get(1).getTitle());
        assertEquals(5, dtos.get(0).getSellerItemsCount());
        assertEquals(3, dtos.get(1).getSellerItemsCount());

        verify(reviewRepository, times(1)).getAverageRatingByUserId(10L);
        verify(reviewRepository, times(1)).getAverageRatingByUserId(20L);
    }

    @Test
    void getAverageRating_Normal_ReturnsRating() {
        when(reviewRepository.getAverageRatingByUserId(1L)).thenReturn(new BigDecimal("4.2"));

        BigDecimal result = converter.getAverageRating(1L);

        assertEquals(new BigDecimal("4.2"), result);
        verify(reviewRepository, times(1)).getAverageRatingByUserId(1L);
    }

    @Test
    void getAverageRating_NoRating_ReturnsNull() {
        when(reviewRepository.getAverageRatingByUserId(1L)).thenReturn(null);

        BigDecimal result = converter.getAverageRating(1L);

        assertNull(result);
        verify(reviewRepository, times(1)).getAverageRatingByUserId(1L);
    }
}
