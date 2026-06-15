package com.idleitems.school.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class DataEncryptionUtilTest {

    private DataEncryptionUtil encryptionUtil;

    @BeforeEach
    void setUp() {
        encryptionUtil = new DataEncryptionUtil();
        ReflectionTestUtils.setField(encryptionUtil, "secretKey", "test-encryption-key-32-bytes-long!!");
    }

    @Test
    void encryptDecrypt_RoundTrip_ReturnsOriginal() {
        String plainText = "Hello World! This is sensitive data.";
        String encrypted = encryptionUtil.encrypt(plainText);
        String decrypted = encryptionUtil.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptDecrypt_WithChineseCharacters() {
        String plainText = "这是一段需要加密的敏感中文数据！@#¥%……&*";
        String encrypted = encryptionUtil.encrypt(plainText);
        String decrypted = encryptionUtil.decrypt(encrypted);

        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptDecrypt_WithSpecialCharacters() {
        String plainText = "!@#$%^&*()_+-=[]{}|;':\",./<>?~`";
        String encrypted = encryptionUtil.encrypt(plainText);
        String decrypted = encryptionUtil.decrypt(encrypted);

        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptDecrypt_WithNumbers() {
        String plainText = "1234567890";
        String encrypted = encryptionUtil.encrypt(plainText);
        String decrypted = encryptionUtil.decrypt(encrypted);

        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptDecrypt_WithEmptyString() {
        String plainText = "";
        String encrypted = encryptionUtil.encrypt(plainText);
        String decrypted = encryptionUtil.decrypt(encrypted);

        assertEquals("", encrypted);
        assertEquals("", decrypted);
    }

    @Test
    void encryptDecrypt_WithNull() {
        assertNull(encryptionUtil.encrypt(null));
        assertNull(encryptionUtil.decrypt(null));
    }

    @Test
    void encrypt_ProducesDifferentCiphertextsForSamePlaintext() {
        String plainText = "consistent data";
        String encrypted1 = encryptionUtil.encrypt(plainText);
        String encrypted2 = encryptionUtil.encrypt(plainText);

        assertNotNull(encrypted1);
        assertNotNull(encrypted2);
        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void decrypt_WithInvalidBase64_ThrowsException() {
        assertThrows(RuntimeException.class, () ->
                encryptionUtil.decrypt("not-valid-base64!!!"));
    }

    @Test
    void decrypt_WithTooShortInput_ThrowsException() {
        assertThrows(RuntimeException.class, () ->
                encryptionUtil.decrypt("c29tZXRoaW5n"));
    }

    @Test
    void decrypt_WithTamperedCiphertext_ThrowsException() {
        String encrypted = encryptionUtil.encrypt("original data");
        String tampered = encrypted.substring(0, encrypted.length() - 4) + "XXXX";

        assertThrows(RuntimeException.class, () ->
                encryptionUtil.decrypt(tampered));
    }

    @Test
    void encryptDecrypt_LongText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("这是一段很长的文本用于测试加密解密的功能是否正常。");
        }
        String longText = sb.toString();

        String encrypted = encryptionUtil.encrypt(longText);
        String decrypted = encryptionUtil.decrypt(encrypted);

        assertEquals(longText, decrypted);
    }

    @Test
    void encryptDecrypt_MultipleRounds() {
        String[] testData = {
                "user1",
                "email@example.com",
                "{\"key\": \"value\"}",
                "<script>alert('xss')</script>",
                "张三丰",
                "a"
        };

        for (String data : testData) {
            String encrypted = encryptionUtil.encrypt(data);
            String decrypted = encryptionUtil.decrypt(encrypted);
            assertEquals(data, decrypted, "Round-trip failed for: " + data);
        }
    }

    @Test
    void encryptDecrypt_WithDifferentKeys_ProducesDifferentResults() {
        DataEncryptionUtil util2 = new DataEncryptionUtil();
        ReflectionTestUtils.setField(util2, "secretKey", "different-encryption-key-here-32bytes!");

        String plainText = "secret message";
        String encrypted1 = encryptionUtil.encrypt(plainText);
        String encrypted2 = util2.encrypt(plainText);

        assertThrows(RuntimeException.class, () ->
                util2.decrypt(encrypted1));
    }
}
