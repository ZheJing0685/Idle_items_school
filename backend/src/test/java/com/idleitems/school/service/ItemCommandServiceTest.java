package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.module.category.entity.Category;
import com.idleitems.school.module.category.repository.CategoryRepository;
import com.idleitems.school.module.item.dto.CreateItemRequest;
import com.idleitems.school.module.item.dto.UpdateItemRequest;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.entity.ItemImage;
import com.idleitems.school.module.item.repository.ItemImageRepository;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.item.service.ItemCommandService;
import com.idleitems.school.module.notification.service.NotificationService;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.user.repository.UserRepository;
import com.idleitems.school.shared.cache.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemCommandServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemImageRepository itemImageRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CacheService cacheService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemCommandService itemCommandService;

    private Item testItem;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setUserId(10L);
        testItem.setTitle("测试物品标题");
        testItem.setDescription("这是一个测试物品的描述信息");
        testItem.setPrice(BigDecimal.valueOf(100));
        testItem.setCategoryId(100L);
        testItem.setStatus(Item.ItemStatus.PENDING);
        testItem.setCondition(Item.ItemCondition.GOOD);
        testItem.setDeliveryMethod("快递");
        testItem.setContactType("微信");
        testItem.setIsBargainAllowed(true);
        testItem.setLocation("北京");
        testItem.setBrand("测试品牌");
        testItem.setCoverImage("http://example.com/cover.jpg");

        testCategory = new Category();
        testCategory.setId(100L);
        testCategory.setName("测试分类");
        testCategory.setParentId(10L);
        testCategory.setStatus(true);
    }

    @Test
    void createItem_withValidRequest_createsAndReturnsItem() {
        CreateItemRequest req = new CreateItemRequest();
        req.setTitle("正常物品标题");
        req.setDescription("这是一个正常的物品描述信息内容");
        req.setPrice(BigDecimal.valueOf(100));
        req.setCategoryId(100L);
        req.setCondition("GOOD");
        req.setDeliveryMethod("快递");
        req.setContactType("微信");
        req.setIsBargainAllowed(true);
        req.setLocation("北京");
        req.setBrand("测试品牌");
        req.setImages(List.of("http://example.com/img1.jpg", "http://example.com/img2.jpg"));
        req.setCoverImage("http://example.com/cover.jpg");

        when(categoryRepository.findById(100L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByParentId(100L)).thenReturn(List.of());
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(userRepository.findByRole(eq(User.Role.ADMIN), any())).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));

        Item result = itemCommandService.createItem(10L, req);

        assertNotNull(result);
        assertEquals("正常物品标题", result.getTitle());
        assertEquals(Item.ItemStatus.PENDING, result.getStatus());
        verify(categoryRepository, times(1)).findById(100L);
        verify(categoryRepository, times(1)).findByParentId(100L);
        verify(itemRepository, times(1)).save(any(Item.class));
        verify(itemImageRepository, times(1)).saveAll(anyList());
        verify(cacheService, times(1)).delete("item:hot");
    }

    @Test
    void createItem_withSensitiveWords_throwsBusinessException() {
        CreateItemRequest req = new CreateItemRequest();
        req.setTitle("枪支买卖");
        req.setDescription("这是一个正常的物品描述信息内容");
        req.setPrice(BigDecimal.valueOf(100));
        req.setCategoryId(100L);

        assertThrows(BusinessException.class, () -> {
            itemCommandService.createItem(10L, req);
        });

        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_withValidRequest_updatesAndReturnsItem() {
        UpdateItemRequest req = new UpdateItemRequest();
        req.setTitle("更新后的标题");
        req.setDescription("更新后的物品描述信息内容");
        req.setPrice(BigDecimal.valueOf(150));
        req.setCategoryId(100L);
        req.setCondition("NEW");
        req.setDeliveryMethod("自取");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByParentId(100L)).thenReturn(List.of());
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        Item result = itemCommandService.updateItem(10L, 1L, req);

        assertNotNull(result);
        verify(itemRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).findById(100L);
        verify(itemRepository, times(1)).save(any(Item.class));
        verify(cacheService, times(1)).delete("item:hot");
    }

    @Test
    void updateItem_whenItemNotFound_throwsBusinessException() {
        UpdateItemRequest req = new UpdateItemRequest();
        req.setTitle("新标题");

        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            itemCommandService.updateItem(10L, 999L, req);
        });

        assertEquals(ErrorCode.ITEM_NOT_FOUND, ex.getErrorCode());
        verify(itemRepository, times(1)).findById(999L);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_whenNoPermission_throwsBusinessException() {
        UpdateItemRequest req = new UpdateItemRequest();
        req.setTitle("新标题");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            itemCommandService.updateItem(99L, 1L, req);
        });

        assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ex.getErrorCode());
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void deleteItemByUser_withValidRequest_deletesItem() {
        testItem.setStatus(Item.ItemStatus.PENDING);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        itemCommandService.deleteItemByUser(10L, 1L);

        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).delete(testItem);
        verify(cacheService, times(1)).delete("item:hot");
    }

    @Test
    void deleteItemByUser_whenItemNotFound_throwsBusinessException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            itemCommandService.deleteItemByUser(10L, 999L);
        });

        assertEquals(ErrorCode.ITEM_NOT_FOUND, ex.getErrorCode());
        verify(itemRepository, times(1)).findById(999L);
        verify(itemRepository, never()).delete(any(Item.class));
    }

    @Test
    void deleteItemByUser_whenNoPermission_throwsBusinessException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            itemCommandService.deleteItemByUser(99L, 1L);
        });

        assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ex.getErrorCode());
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, never()).delete(any(Item.class));
    }

    @Test
    void offShelfItem_withValidRequest_offShelvesItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        Item result = itemCommandService.offShelfItem(10L, 1L);

        assertNotNull(result);
        assertEquals(Item.ItemStatus.OFF_SHELF, result.getStatus());
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).save(any(Item.class));
        verify(cacheService, times(1)).delete("item:hot");
    }
}
