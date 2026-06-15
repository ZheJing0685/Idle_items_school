package com.idleitems.school.fixtures;

import com.idleitems.school.module.item.entity.Item;
import java.math.BigDecimal;

/**
 * 物品测试数据工厂
 */
public class ItemFixture {
    
    public static Item createPhone(Long userId) {
        Item item = new Item();
        item.setTitle("iPhone 15 Pro");
        item.setDescription("全新未拆封，国行正品");
        item.setPrice(new BigDecimal("7999.00"));
        item.setOriginalPrice(new BigDecimal("8999.00"));
        item.setCondition(Item.ItemCondition.NEW);
        item.setDeliveryMethod("面交");
        item.setLocation("北京大学");
        item.setCategoryId(1L);
        item.setUserId(userId);
        return item;
    }
    
    public static Item createLaptop(Long userId) {
        Item item = new Item();
        item.setTitle("MacBook Pro 14寸");
        item.setDescription("M3 Pro芯片，16GB内存");
        item.setPrice(new BigDecimal("14999.00"));
        item.setOriginalPrice(new BigDecimal("16999.00"));
        item.setCondition(Item.ItemCondition.LIKE_NEW);
        item.setDeliveryMethod("快递");
        item.setLocation("清华大学");
        item.setCategoryId(1L);
        item.setUserId(userId);
        return item;
    }
    
    public static Item createBook(Long userId) {
        Item item = new Item();
        item.setTitle("高等数学（同济版）");
        item.setDescription("九成新，有少量笔记");
        item.setPrice(new BigDecimal("25.00"));
        item.setOriginalPrice(new BigDecimal("45.00"));
        item.setCondition(Item.ItemCondition.GOOD);
        item.setDeliveryMethod("面交");
        item.setLocation("北京大学图书馆");
        item.setCategoryId(2L);
        item.setUserId(userId);
        return item;
    }
    
    public static Item createWithCustomData(Long userId, String title, BigDecimal price, Item.ItemCondition condition) {
        Item item = new Item();
        item.setTitle(title);
        item.setDescription("测试物品描述");
        item.setPrice(price);
        item.setOriginalPrice(price.multiply(new BigDecimal("1.2")));
        item.setCondition(condition);
        item.setDeliveryMethod("面交");
        item.setLocation("测试地点");
        item.setCategoryId(1L);
        item.setUserId(userId);
        return item;
    }
}
