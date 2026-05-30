package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.dto.CreateItemRequest;
import com.idleitems.school.dto.UpdateItemRequest;
import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.ItemImage;
import com.idleitems.school.repository.ItemImageRepository;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.cache.CacheService;
import com.idleitems.school.util.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemCommandService {

    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;
    private final CacheService cacheService;

    @Transactional
    public Item createItem(Long userId, CreateItemRequest req) {
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

        if (images != null && !images.isEmpty()) {
            saveItemImages(savedItem.getId(), images, req.getCoverImage());
        }

        clearItemCache();
        return savedItem;
    }

    @Transactional
    public Item updateItem(Long userId, Long itemId, UpdateItemRequest req) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));

        if (!existingItem.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "No permission");
        }

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
        clearItemCache();
        return updatedItem;
    }

    @Transactional
    public Item offShelfItem(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));

        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "No permission");
        }

        item.setStatus(Item.ItemStatus.OFF_SHELF);
        Item updatedItem = itemRepository.save(item);
        clearItemCache();
        return updatedItem;
    }

    @Transactional
    public Item onShelfItem(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));

        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "No permission");
        }

        if (item.getStatus() != Item.ItemStatus.OFF_SHELF && item.getStatus() != Item.ItemStatus.DRAFT) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Only off-shelf or draft items can be listed");
        }

        item.setStatus(Item.ItemStatus.ON_SALE);
        Item updatedItem = itemRepository.save(item);
        clearItemCache();
        return updatedItem;
    }

    @Transactional
    public void deleteItemByUser(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Item not found"));

        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission");
        }

        if (item.getStatus() == Item.ItemStatus.SOLD) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Cannot delete sold items");
        }

        itemRepository.delete(item);
        clearItemCache();
    }

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

    private void checkSensitiveWords(String title, String description) {
        List<String> words = new ArrayList<>();
        words.addAll(SensitiveWordFilter.findSensitiveWords(title));
        words.addAll(SensitiveWordFilter.findSensitiveWords(description));
        if (!words.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, SensitiveWordFilter.getWarningMessage(words));
        }
    }

    private void clearItemCache() {
        cacheService.deletePattern("item:list:*");
        cacheService.deletePattern("item:hot");
        cacheService.delete("categories:all");
        cacheService.delete("categories:tree");
    }
}
