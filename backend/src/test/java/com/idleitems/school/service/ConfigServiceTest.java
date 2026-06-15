package com.idleitems.school.service;

import com.idleitems.school.module.system.entity.SystemConfig;
import com.idleitems.school.module.system.repository.SystemConfigRepository;
import com.idleitems.school.module.system.service.ConfigService;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigServiceTest {

    @Mock
    private SystemConfigRepository systemConfigRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private ConfigService configService;

    private SystemConfig textConfig;
    private SystemConfig numberConfig;
    private SystemConfig booleanConfig;

    @BeforeEach
    void setUp() {
        textConfig = new SystemConfig();
        textConfig.setId(1L);
        textConfig.setConfigKey("site.name");
        textConfig.setConfigValue("闲鱼校园版");
        textConfig.setConfigType(1);
        textConfig.setDescription("站点名称");
        textConfig.setGroupName("general");

        numberConfig = new SystemConfig();
        numberConfig.setId(2L);
        numberConfig.setConfigKey("site.max_items");
        numberConfig.setConfigValue("100");
        numberConfig.setConfigType(2);
        numberConfig.setDescription("最大物品数");
        numberConfig.setGroupName("general");

        booleanConfig = new SystemConfig();
        booleanConfig.setId(3L);
        booleanConfig.setConfigKey("site.registration_open");
        booleanConfig.setConfigValue("true");
        booleanConfig.setConfigType(3);
        booleanConfig.setDescription("注册开放");
        booleanConfig.setGroupName("auth");
    }

    @Test
    void getAllConfigs_FromCache_ReturnsCached() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Map<String, Object> cachedConfigs = Map.of("site.name", "闲鱼校园版");
        when(valueOperations.get("config:all")).thenReturn(cachedConfigs);

        Map<String, Object> result = configService.getAllConfigs();

        assertNotNull(result);
        assertEquals("闲鱼校园版", result.get("site.name"));
        verify(systemConfigRepository, never()).findAll();
    }

    @Test
    void getAllConfigs_NoCache_LoadsFromDb() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:all")).thenReturn(null);
        when(systemConfigRepository.findAll()).thenReturn(List.of(textConfig, numberConfig));

        Map<String, Object> result = configService.getAllConfigs();

        assertNotNull(result);
        assertEquals("闲鱼校园版", result.get("site.name"));
        assertEquals(100L, result.get("site.max_items"));
        verify(valueOperations, times(1)).set(eq("config:all"), anyMap(), eq(1L), eq(TimeUnit.HOURS));
    }

    @Test
    void getConfig_FromCache_ReturnsCached() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:site.name")).thenReturn("闲鱼校园版");

        Object result = configService.getConfig("site.name");

        assertEquals("闲鱼校园版", result);
        verify(systemConfigRepository, never()).findByConfigKey(anyString());
    }

    @Test
    void getConfig_NoCache_LoadsFromDb() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:site.name")).thenReturn(null);
        when(systemConfigRepository.findByConfigKey("site.name")).thenReturn(Optional.of(textConfig));

        Object result = configService.getConfig("site.name");

        assertEquals("闲鱼校园版", result);
        verify(valueOperations, times(1)).set(eq("config:site.name"), eq("闲鱼校园版"), eq(1L), eq(TimeUnit.HOURS));
    }

    @Test
    void getConfig_NotExists_CachesNullSentinel() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:nonexistent")).thenReturn(null);
        when(systemConfigRepository.findByConfigKey("nonexistent")).thenReturn(Optional.empty());

        Object result = configService.getConfig("nonexistent");

        assertNull(result);
        verify(valueOperations, times(1)).set(eq("config:nonexistent"), eq("NULL_SENTINEL"), eq(5L), eq(TimeUnit.MINUTES));
    }

    @Test
    void getConfig_WhenCacheHasNullSentinel_ReturnsNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:deleted_key")).thenReturn("NULL_SENTINEL");

        Object result = configService.getConfig("deleted_key");

        assertNull(result);
        verify(systemConfigRepository, never()).findByConfigKey(anyString());
    }

    @Test
    void getConfigValue_ReturnsStringValue() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:site.name")).thenReturn("闲鱼校园版");

        String result = configService.getConfigValue("site.name");

        assertEquals("闲鱼校园版", result);
    }

    @Test
    void getConfigValue_WhenNull_ReturnsNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:missing")).thenReturn(null);
        when(systemConfigRepository.findByConfigKey("missing")).thenReturn(Optional.empty());

        String result = configService.getConfigValue("missing");

        assertNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void saveConfig_NewConfig_SavesAndClearsCache() {
        when(systemConfigRepository.findByConfigKey("new.key")).thenReturn(Optional.empty());

        SystemConfig savedConfig = new SystemConfig();
        savedConfig.setId(10L);
        savedConfig.setConfigKey("new.key");
        savedConfig.setConfigValue("new_value");
        when(systemConfigRepository.save(any(SystemConfig.class))).thenReturn(savedConfig);

        Cursor<String> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(false);
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);

        SystemConfig result = configService.saveConfig("new.key", "new_value", "新配置");

        assertNotNull(result);
        assertEquals("new.key", result.getConfigKey());
        assertEquals("new_value", result.getConfigValue());
        verify(systemConfigRepository, times(1)).save(any(SystemConfig.class));
        verify(redisTemplate, times(1)).delete("config:all");
    }

    @SuppressWarnings("unchecked")
    @Test
    void saveConfig_ExistingConfig_UpdatesAndClearsCache() {
        SystemConfig existing = new SystemConfig();
        existing.setId(1L);
        existing.setConfigKey("site.name");
        existing.setConfigValue("旧名称");
        existing.setDescription("旧描述");
        when(systemConfigRepository.findByConfigKey("site.name")).thenReturn(Optional.of(existing));

        SystemConfig updatedConfig = new SystemConfig();
        updatedConfig.setId(1L);
        updatedConfig.setConfigKey("site.name");
        updatedConfig.setConfigValue("新名称");
        updatedConfig.setDescription("新描述");
        when(systemConfigRepository.save(any(SystemConfig.class))).thenReturn(updatedConfig);

        Cursor<String> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(false);
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);

        SystemConfig result = configService.saveConfig("site.name", "新名称", "新描述");

        assertEquals("新名称", result.getConfigValue());
        verify(systemConfigRepository, times(1)).save(any(SystemConfig.class));
        verify(redisTemplate, times(1)).delete("config:all");
    }

    @SuppressWarnings("unchecked")
    @Test
    void deleteConfig_ExistingConfig_DeletesAndClearsCache() {
        when(systemConfigRepository.findByConfigKey("delete.key")).thenReturn(Optional.of(textConfig));
        doNothing().when(systemConfigRepository).delete(textConfig);

        Cursor<String> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(false);
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);

        boolean result = configService.deleteConfig("delete.key");

        assertTrue(result);
        verify(systemConfigRepository, times(1)).delete(textConfig);
        verify(redisTemplate, times(1)).delete("config:all");
    }

    @Test
    void deleteConfig_NonExisting_ReturnsFalse() {
        when(systemConfigRepository.findByConfigKey("nonexistent")).thenReturn(Optional.empty());

        boolean result = configService.deleteConfig("nonexistent");

        assertFalse(result);
        verify(systemConfigRepository, never()).delete(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getConfigsByGroup_FromCache_ReturnsCached() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Map<String, Object> cachedGroup = Map.of("site.name", "闲鱼校园版");
        when(valueOperations.get("config:group:general")).thenReturn(cachedGroup);

        Map<String, Object> result = configService.getConfigsByGroup("general");

        assertNotNull(result);
        assertEquals("闲鱼校园版", result.get("site.name"));
        verify(systemConfigRepository, never()).findAll();
    }

    @SuppressWarnings("unchecked")
    @Test
    void getConfigsByGroup_NoCache_ReturnsGroupedConfigs() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        SystemConfig otherGroup = new SystemConfig();
        otherGroup.setId(4L);
        otherGroup.setConfigKey("auth.token_expire");
        otherGroup.setConfigValue("3600");
        otherGroup.setConfigType(2);
        otherGroup.setGroupName("auth");

        when(valueOperations.get("config:group:auth")).thenReturn(null);
        when(systemConfigRepository.findAll()).thenReturn(List.of(textConfig, numberConfig, booleanConfig, otherGroup));

        Map<String, Object> result = configService.getConfigsByGroup("auth");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue((Boolean) result.get("site.registration_open"));
        assertEquals(3600L, result.get("auth.token_expire"));
        verify(valueOperations, times(1)).set(eq("config:group:auth"), anyMap(), eq(1L), eq(TimeUnit.HOURS));
    }

    @Test
    void getConfigInt_whenValueIsInteger_returnsValue() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.int")).thenReturn(42);

        Integer result = configService.getConfigInt("test.int");

        assertEquals(42, result);
        verify(systemConfigRepository, never()).findByConfigKey(anyString());
    }

    @Test
    void getConfigInt_whenValueIsString_parsesSuccess() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.int")).thenReturn("100");

        Integer result = configService.getConfigInt("test.int");

        assertEquals(100, result);
    }

    @Test
    void getConfigInt_whenValueIsString_parseFails() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.int")).thenReturn("not_a_number");

        Integer result = configService.getConfigInt("test.int");

        assertNull(result);
    }

    @Test
    void getConfigInt_whenValueIsOtherType_returnsNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.int")).thenReturn(3.14);

        Integer result = configService.getConfigInt("test.int");

        assertNull(result);
    }

    @Test
    void getConfigInt_whenValueIsNull_returnsNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.int")).thenReturn(null);
        when(systemConfigRepository.findByConfigKey("test.int")).thenReturn(Optional.empty());

        Integer result = configService.getConfigInt("test.int");

        assertNull(result);
    }

    @Test
    void getConfigLong_whenValueIsLong_returnsValue() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.long")).thenReturn(100L);

        Long result = configService.getConfigLong("test.long");

        assertEquals(100L, result);
    }

    @Test
    void getConfigLong_whenValueIsInteger_convertsToLong() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.long")).thenReturn(50);

        Long result = configService.getConfigLong("test.long");

        assertEquals(50L, result);
    }

    @Test
    void getConfigLong_whenValueIsString_parsesSuccess() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.long")).thenReturn("200");

        Long result = configService.getConfigLong("test.long");

        assertEquals(200L, result);
    }

    @Test
    void getConfigLong_whenValueIsString_parseFails() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.long")).thenReturn("bad");

        Long result = configService.getConfigLong("test.long");

        assertNull(result);
    }

    @Test
    void getConfigLong_whenValueIsOtherType_returnsNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.long")).thenReturn(true);

        Long result = configService.getConfigLong("test.long");

        assertNull(result);
    }

    @Test
    void getConfigBoolean_whenValueIsBooleanTrue_returnsTrue() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.bool")).thenReturn(true);

        Boolean result = configService.getConfigBoolean("test.bool");

        assertTrue(result);
    }

    @Test
    void getConfigBoolean_whenValueIsBooleanFalse_returnsFalse() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.bool")).thenReturn(false);

        Boolean result = configService.getConfigBoolean("test.bool");

        assertFalse(result);
    }

    @Test
    void getConfigBoolean_whenValueIsStringTrue_returnsTrue() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.bool")).thenReturn("true");

        Boolean result = configService.getConfigBoolean("test.bool");

        assertTrue(result);
    }

    @Test
    void getConfigBoolean_whenValueIsStringFalse_returnsFalse() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.bool")).thenReturn("false");

        Boolean result = configService.getConfigBoolean("test.bool");

        assertFalse(result);
    }

    @Test
    void getConfigBoolean_whenValueIsOther_returnsNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.bool")).thenReturn(123);

        Boolean result = configService.getConfigBoolean("test.bool");

        assertNull(result);
    }

    @Test
    void getConfigFloat_whenValueIsFloat_returnsValue() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.float")).thenReturn(1.5f);

        Float result = configService.getConfigFloat("test.float");

        assertEquals(1.5f, result, 0.001f);
    }

    @Test
    void getConfigFloat_whenValueIsDouble_convertsToFloat() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.float")).thenReturn(2.5);

        Float result = configService.getConfigFloat("test.float");

        assertEquals(2.5f, result, 0.001f);
    }

    @Test
    void getConfigFloat_whenValueIsString_parsesSuccess() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.float")).thenReturn("3.14");

        Float result = configService.getConfigFloat("test.float");

        assertEquals(3.14f, result, 0.001f);
    }

    @Test
    void getConfigFloat_whenValueIsString_parseFails() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.float")).thenReturn("not_float");

        Float result = configService.getConfigFloat("test.float");

        assertNull(result);
    }

    @Test
    void getConfigFloat_whenValueIsOtherType_returnsNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:test.float")).thenReturn(true);

        Float result = configService.getConfigFloat("test.float");

        assertNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void saveConfig_newConfig_withDescriptionNull() {
        when(systemConfigRepository.findByConfigKey("new.key")).thenReturn(Optional.empty());

        SystemConfig savedConfig = new SystemConfig();
        savedConfig.setId(10L);
        savedConfig.setConfigKey("new.key");
        savedConfig.setConfigValue("val");
        when(systemConfigRepository.save(any(SystemConfig.class))).thenReturn(savedConfig);

        Cursor<String> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(false);
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);

        SystemConfig result = configService.saveConfig("new.key", "val", null);

        assertNotNull(result);
        assertNull(result.getDescription());
        verify(redisTemplate, times(1)).delete("config:all");
    }

    @SuppressWarnings("unchecked")
    @Test
    void saveConfig_existingConfig_withDescriptionNull_keepsOldDescription() {
        SystemConfig existing = new SystemConfig();
        existing.setId(1L);
        existing.setConfigKey("key");
        existing.setConfigValue("old");
        existing.setDescription("原始描述");
        when(systemConfigRepository.findByConfigKey("key")).thenReturn(Optional.of(existing));

        SystemConfig updated = new SystemConfig();
        updated.setId(1L);
        updated.setConfigKey("key");
        updated.setConfigValue("new");
        updated.setDescription("原始描述");
        when(systemConfigRepository.save(any(SystemConfig.class))).thenReturn(updated);

        Cursor<String> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(false);
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);

        SystemConfig result = configService.saveConfig("key", "new", null);

        assertEquals("原始描述", result.getDescription());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getConfigsByGroup_fromDb_withMatchingGroup() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:group:auth")).thenReturn(null);
        when(systemConfigRepository.findAll()).thenReturn(List.of(textConfig, numberConfig, booleanConfig));

        Map<String, Object> result = configService.getConfigsByGroup("auth");

        assertNotNull(result);
        assertTrue(result.containsKey("site.registration_open"));
        assertTrue((Boolean) result.get("site.registration_open"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAllConfigs_fromDb_parsesMultipleTypes() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:all")).thenReturn(null);
        when(systemConfigRepository.findAll()).thenReturn(List.of(textConfig, numberConfig, booleanConfig));

        Map<String, Object> result = configService.getAllConfigs();

        assertNotNull(result);
        assertEquals("闲鱼校园版", result.get("site.name"));
        assertEquals(100L, result.get("site.max_items"));
        assertTrue((Boolean) result.get("site.registration_open"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void parseConfigValue_numberWithDecimal() {
        SystemConfig config = new SystemConfig();
        config.setConfigKey("decimal.val");
        config.setConfigValue("3.14");
        config.setConfigType(2);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:all")).thenReturn(null);
        when(systemConfigRepository.findAll()).thenReturn(List.of(config));

        Map<String, Object> result = configService.getAllConfigs();

        assertEquals(3.14, (Double) result.get("decimal.val"), 0.001);
    }

    @SuppressWarnings("unchecked")
    @Test
    void parseConfigValue_numberParseFails_returnsRawString() {
        SystemConfig config = new SystemConfig();
        config.setConfigKey("bad.num");
        config.setConfigValue("12xyz");
        config.setConfigType(2);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:all")).thenReturn(null);
        when(systemConfigRepository.findAll()).thenReturn(List.of(config));

        Map<String, Object> result = configService.getAllConfigs();

        assertEquals("12xyz", result.get("bad.num"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void parseConfigValue_jsonType() {
        SystemConfig config = new SystemConfig();
        config.setConfigKey("json.data");
        config.setConfigValue("{\"key\":\"val\"}");
        config.setConfigType(4);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("config:all")).thenReturn(null);
        when(systemConfigRepository.findAll()).thenReturn(List.of(config));

        Map<String, Object> result = configService.getAllConfigs();

        assertEquals("{\"key\":\"val\"}", result.get("json.data"));
    }

}
