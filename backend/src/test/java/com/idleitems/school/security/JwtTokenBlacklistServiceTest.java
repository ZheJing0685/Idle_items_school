package com.idleitems.school.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenBlacklistServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private JwtTokenBlacklistService blacklistService;

    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMTIzIn0.signature";

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void addToBlacklist_WithPositiveExpiration_SetsWithTtl() {
        blacklistService.addToBlacklist(TEST_TOKEN, 3600000L);

        verify(valueOperations, times(1))
                .set(startsWith("token:blacklist:"), eq("1"), eq(3600000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void addToBlacklist_WithZeroExpiration_SetsWithOneMinuteTtl() {
        blacklistService.addToBlacklist(TEST_TOKEN, 0L);

        verify(valueOperations, times(1))
                .set(startsWith("token:blacklist:"), eq("1"), eq(1L), eq(TimeUnit.MINUTES));
    }

    @Test
    void addToBlacklist_WithNegativeExpiration_SetsWithOneMinuteTtl() {
        blacklistService.addToBlacklist(TEST_TOKEN, -1L);

        verify(valueOperations, times(1))
                .set(startsWith("token:blacklist:"), eq("1"), eq(1L), eq(TimeUnit.MINUTES));
    }

    @Test
    void addToBlacklistForUser_SetsBothKeys() {
        blacklistService.addToBlacklistForUser(TEST_TOKEN, 100L, 3600000L);

        verify(valueOperations, times(2))
                .set(anyString(), eq("1"), eq(3600000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void addToBlacklistForUser_WithZeroExpiration_SetsWithDefaultTtl() {
        blacklistService.addToBlacklistForUser(TEST_TOKEN, 100L, 0L);

        verify(valueOperations, times(2))
                .set(anyString(), eq("1"), eq(60000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void isBlacklisted_WhenTokenIsBlacklisted_ReturnsTrue() {
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        assertTrue(blacklistService.isBlacklisted(TEST_TOKEN));
        verify(redisTemplate, times(1)).hasKey(anyString());
    }

    @Test
    void isBlacklisted_WhenTokenIsNotBlacklisted_ReturnsFalse() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        assertFalse(blacklistService.isBlacklisted(TEST_TOKEN));
        verify(redisTemplate, times(1)).hasKey(anyString());
    }

    @Test
    void isBlacklisted_WhenRedisReturnsNull_ReturnsFalse() {
        when(redisTemplate.hasKey(anyString())).thenReturn(null);

        assertFalse(blacklistService.isBlacklisted(TEST_TOKEN));
    }

    @Test
    void invalidateAllUserTokens_IncrementsVersionAndScansKeys() {
        when(valueOperations.increment(anyString())).thenReturn(5L);
        when(redisTemplate.expire(anyString(), eq(7L), eq(TimeUnit.DAYS))).thenReturn(true);

        @SuppressWarnings("unchecked")
        Cursor<String> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(false);
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);

        blacklistService.invalidateAllUserTokens(100L);

        verify(valueOperations, times(1)).increment("token:user_version:100");
        verify(redisTemplate, times(1)).expire("token:user_version:100", 7, TimeUnit.DAYS);
        verify(redisTemplate, times(1)).scan(any(ScanOptions.class));
    }

    @Test
    void invalidateAllUserTokens_WithKeysToDelete_DeletesKeys() {
        when(valueOperations.increment(anyString())).thenReturn(3L);
        when(redisTemplate.expire(anyString(), eq(7L), eq(TimeUnit.DAYS))).thenReturn(true);

        @SuppressWarnings("unchecked")
        Cursor<String> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn("token:blacklist:user:100:abc", "token:blacklist:user:100:def");

        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);

        blacklistService.invalidateAllUserTokens(100L);

        verify(redisTemplate, times(1)).delete(Arrays.asList("token:blacklist:user:100:abc", "token:blacklist:user:100:def"));
    }

    @Test
    void invalidateAllUserTokens_WhenIncrementReturnsNull_StillScans() {
        when(valueOperations.increment(anyString())).thenReturn(null);

        @SuppressWarnings("unchecked")
        Cursor<String> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(false);
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);

        blacklistService.invalidateAllUserTokens(100L);

        verify(redisTemplate, never()).expire(anyString(), anyLong(), any());
    }

    @Test
    void getUserTokenVersion_WhenKeyExists_ReturnsVersion() {
        when(valueOperations.get("token:user_version:100")).thenReturn("7");

        long version = blacklistService.getUserTokenVersion(100L);
        assertEquals(7L, version);
    }

    @Test
    void getUserTokenVersion_WhenKeyNotExists_ReturnsZero() {
        when(valueOperations.get("token:user_version:100")).thenReturn(null);

        long version = blacklistService.getUserTokenVersion(100L);
        assertEquals(0L, version);
    }

    @Test
    void getUserTokenVersion_WhenValueIsNotNumber_ReturnsZero() {
        when(valueOperations.get("token:user_version:100")).thenReturn("not_a_number");

        long version = blacklistService.getUserTokenVersion(100L);
        assertEquals(0L, version);
    }

    @Test
    void addToBlacklist_DifferentTokens_UseDifferentHashes() {
        String token2 = "different.token.value";

        blacklistService.addToBlacklist(TEST_TOKEN, 3600000L);
        blacklistService.addToBlacklist(token2, 3600000L);

        verify(valueOperations, times(2))
                .set(anyString(), eq("1"), eq(3600000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void scanKeys_WhenExceptionOccurs_ReturnsEmptyList() {
        when(redisTemplate.scan(any(ScanOptions.class))).thenThrow(new RuntimeException("Redis error"));

        blacklistService.invalidateAllUserTokens(100L);

        verify(redisTemplate, never()).delete(anyList());
    }
}
