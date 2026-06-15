package com.idleitems.school.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataMaskUtilTest {

    @Test
    void maskPhone_Normal_ReturnsMasked() {
        assertEquals("138****5678", DataMaskUtil.maskPhone("13812345678"));
    }

    @Test
    void maskPhone_Short_ReturnsOriginal() {
        String shortPhone = "123456";
        assertSame(shortPhone, DataMaskUtil.maskPhone(shortPhone));
    }

    @Test
    void maskPhone_Null_ReturnsNull() {
        assertNull(DataMaskUtil.maskPhone(null));
    }

    @Test
    void maskIdCard_Normal_ReturnsMasked() {
        assertEquals("1101**********1234", DataMaskUtil.maskIdCard("110101199901011234"));
    }

    @Test
    void maskIdCard_Short_ReturnsOriginal() {
        String shortCard = "1234567";
        assertSame(shortCard, DataMaskUtil.maskIdCard(shortCard));
    }

    @Test
    void maskIdCard_Null_ReturnsNull() {
        assertNull(DataMaskUtil.maskIdCard(null));
    }

    @Test
    void maskEmail_Normal_ReturnsMasked() {
        assertEquals("zh****@example.com", DataMaskUtil.maskEmail("zhangsan@example.com"));
    }

    @Test
    void maskEmail_NoAt_ReturnsOriginal() {
        String noAt = "notanemail";
        assertSame(noAt, DataMaskUtil.maskEmail(noAt));
    }

    @Test
    void maskEmail_ShortPrefix_ReturnsOriginal() {
        String shortPrefix = "ab@x.com";
        assertSame(shortPrefix, DataMaskUtil.maskEmail(shortPrefix));
    }

    @Test
    void maskEmail_Null_ReturnsNull() {
        assertNull(DataMaskUtil.maskEmail(null));
    }

    @Test
    void maskName_SingleChar_ReturnsOriginal() {
        String single = "张";
        assertSame(single, DataMaskUtil.maskName(single));
    }

    @Test
    void maskName_TwoChars_ReturnsFirstAndStar() {
        assertEquals("张*", DataMaskUtil.maskName("张三"));
    }

    @Test
    void maskName_ThreeChars_ReturnsFirstStarLast() {
        assertEquals("张*丰", DataMaskUtil.maskName("张三丰"));
    }

    @Test
    void maskName_FourChars_ReturnsFirstTwoStarsLast() {
        assertEquals("张**丰", DataMaskUtil.maskName("张三三丰"));
    }

    @Test
    void maskName_Null_ReturnsNull() {
        assertNull(DataMaskUtil.maskName(null));
    }

    @Test
    void maskName_Empty_ReturnsEmpty() {
        assertEquals("", DataMaskUtil.maskName(""));
    }

    @Test
    void maskBankCard_Normal_ReturnsMasked() {
        assertEquals("6222****0123", DataMaskUtil.maskBankCard("6222021234567890123"));
    }

    @Test
    void maskBankCard_Short_ReturnsOriginal() {
        String shortCard = "1234567";
        assertSame(shortCard, DataMaskUtil.maskBankCard(shortCard));
    }

    @Test
    void maskBankCard_Null_ReturnsNull() {
        assertNull(DataMaskUtil.maskBankCard(null));
    }

    @Test
    void maskAddress_Normal_ReturnsMasked() {
        assertEquals("北京市海淀区****", DataMaskUtil.maskAddress("北京市海淀区中关村大街1号"));
    }

    @Test
    void maskAddress_Short_ReturnsOriginal() {
        String shortAddr = "北京";
        assertSame(shortAddr, DataMaskUtil.maskAddress(shortAddr));
    }

    @Test
    void maskAddress_Null_ReturnsNull() {
        assertNull(DataMaskUtil.maskAddress(null));
    }

    @Test
    void maskContactInfo_Normal_ReturnsMasked() {
        assertEquals("wec****45", DataMaskUtil.maskContactInfo("wechat12345"));
    }

    @Test
    void maskContactInfo_Short_ReturnsOriginal() {
        String shortInfo = "abc";
        assertSame(shortInfo, DataMaskUtil.maskContactInfo(shortInfo));
    }

    @Test
    void maskContactInfo_Null_ReturnsNull() {
        assertNull(DataMaskUtil.maskContactInfo(null));
    }
}
