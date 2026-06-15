package com.idleitems.school.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderNoGeneratorTest {

    private static final String ALLOWED_RANDOM_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    @Test
    void generate_StartsWithORD() {
        String orderNo = OrderNoGenerator.generate();
        assertTrue(orderNo.startsWith("ORD"));
    }

    @Test
    void generate_HasCorrectLength() {
        String orderNo = OrderNoGenerator.generate();
        assertEquals(29, orderNo.length());
    }

    @Test
    void generate_ContainsOnlyDigitsInNumericSection() {
        String orderNo = OrderNoGenerator.generate();
        String numericPart = orderNo.substring(17, 25);
        assertTrue(numericPart.chars().allMatch(Character::isDigit));
    }

    @Test
    void generate_ContainsOnlyAllowedCharsInRandomSection() {
        String orderNo = OrderNoGenerator.generate();
        String randomPart = orderNo.substring(25);
        assertTrue(randomPart.chars().allMatch(ch -> ALLOWED_RANDOM_CHARS.indexOf(ch) >= 0));
    }

    @Test
    void generate_ReturnsUniqueValues() {
        String orderNo1 = OrderNoGenerator.generate();
        String orderNo2 = OrderNoGenerator.generate();
        assertNotEquals(orderNo1, orderNo2);
    }
}
