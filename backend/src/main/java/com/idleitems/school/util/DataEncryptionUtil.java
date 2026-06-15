package com.idleitems.school.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 数据加密工具类
 * 使用AES-256-GCM算法对敏感数据进行加密解密
 */
@Slf4j
@Component
public class DataEncryptionUtil {

    @Value("${app.encryption.secret-key}")
    private String secretKey;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 加密字符串
     *
     * @param plainText 明文
     * @return 密文（IV + 密文，Base64编码）
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            byte[] keyBytes = deriveKey(secretKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);

            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] ivAndCipherText = new byte[GCM_IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, ivAndCipherText, 0, GCM_IV_LENGTH);
            System.arraycopy(encrypted, 0, ivAndCipherText, GCM_IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(ivAndCipherText);
        } catch (Exception e) {
            log.error("加密失败", e);
            throw new RuntimeException("数据加密失败", e);
        }
    }

    /**
     * 解密字符串
     *
     * @param cipherText 密文（Base64编码，前12字节为IV）
     * @return 明文
     */
    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        try {
            byte[] keyBytes = deriveKey(secretKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);

            byte[] ivAndCipherText = Base64.getDecoder().decode(cipherText);

            if (ivAndCipherText.length < GCM_IV_LENGTH) {
                throw new IllegalArgumentException("密文长度不足");
            }

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(ivAndCipherText, 0, iv, 0, GCM_IV_LENGTH);

            byte[] encrypted = new byte[ivAndCipherText.length - GCM_IV_LENGTH];
            System.arraycopy(ivAndCipherText, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
    } catch (Exception e) {
        log.warn("解密失败: {}（可能是旧数据未加密）", e.getMessage());
        throw new RuntimeException("数据解密失败", e);
    }
    }

    /**
     * 使用SHA-256派生固定长度的AES密钥
     *
     * @param key 原始密钥
     * @return 32字节的AES-256密钥
     */
    private byte[] deriveKey(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(key.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("密钥派生失败", e);
            throw new RuntimeException("密钥派生失败", e);
        }
    }
}
