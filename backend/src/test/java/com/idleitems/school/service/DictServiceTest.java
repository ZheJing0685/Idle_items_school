package com.idleitems.school.service;

import com.idleitems.school.module.system.entity.DictItem;
import com.idleitems.school.module.system.entity.DictType;
import com.idleitems.school.module.system.repository.DictItemRepository;
import com.idleitems.school.module.system.repository.DictTypeRepository;
import com.idleitems.school.module.system.service.DictService;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DictServiceTest {

    @Mock
    private DictTypeRepository dictTypeRepository;

    @Mock
    private DictItemRepository dictItemRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private DictService dictService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getAllDicts_FromCache_ReturnsCached() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Map<String, List<Map<String, Object>>> cachedData = new LinkedHashMap<>();
        when(valueOperations.get("dict:all")).thenReturn(cachedData);

        Map<String, List<Map<String, Object>>> result = dictService.getAllDicts();

        assertSame(cachedData, result);
        verify(dictTypeRepository, never()).findByStatusTrue();
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any());
    }

    @Test
    void getAllDicts_NoCache_LoadsFromDb() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("dict:all")).thenReturn(null);

        DictType dictType = new DictType();
        dictType.setTypeCode("gender");
        dictType.setTypeName("性别");
        when(dictTypeRepository.findByStatusTrue()).thenReturn(List.of(dictType));

        DictItem item = new DictItem();
        item.setItemValue("1");
        item.setItemLabel("男");
        when(dictItemRepository.findByTypeCodeAndStatusTrue("gender")).thenReturn(List.of(item));

        Map<String, List<Map<String, Object>>> result = dictService.getAllDicts();

        assertTrue(result.containsKey("gender"));
        assertEquals("男", result.get("gender").get(0).get("label"));
        verify(valueOperations).set(eq("dict:all"), any(), eq(24L), eq(TimeUnit.HOURS));
    }

    @Test
    void getDictByType_FromCache_ReturnsCached() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        List<Map<String, Object>> cachedList = List.of();
        when(valueOperations.get("dict:gender")).thenReturn(cachedList);

        List<Map<String, Object>> result = dictService.getDictByType("gender");

        assertSame(cachedList, result);
        verify(dictItemRepository, never()).findByTypeCodeAndStatusTrue(anyString());
    }

    @Test
    void getDictByType_NoCache_LoadsFromDb() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("dict:gender")).thenReturn(null);

        DictItem item = new DictItem();
        item.setItemValue("1");
        item.setItemLabel("男");
        when(dictItemRepository.findByTypeCodeAndStatusTrue("gender")).thenReturn(List.of(item));

        List<Map<String, Object>> result = dictService.getDictByType("gender");

        assertEquals(1, result.size());
        assertEquals("男", result.get(0).get("label"));
        verify(valueOperations).set(eq("dict:gender"), any(), eq(24L), eq(TimeUnit.HOURS));
    }

    @Test
    void getDictLabel_FromCache_ReturnsCached() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("dict:gender:1")).thenReturn("男");

        String result = dictService.getDictLabel("gender", "1");

        assertEquals("男", result);
        verify(dictItemRepository, never()).findItemLabelByTypeCodeAndValue(anyString(), anyString());
    }

    @Test
    void getDictLabel_NoCache_LoadsFromDb() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("dict:gender:1")).thenReturn(null);
        when(dictItemRepository.findItemLabelByTypeCodeAndValue("gender", "1")).thenReturn("男");

        String result = dictService.getDictLabel("gender", "1");

        assertEquals("男", result);
        verify(valueOperations).set("dict:gender:1", "男", 24L, TimeUnit.HOURS);
    }

    @Test
    void getDictLabel_NotExists_CachesNullSentinel() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("dict:gender:nonexistent")).thenReturn(null);
        when(dictItemRepository.findItemLabelByTypeCodeAndValue("gender", "nonexistent")).thenReturn(null);

        String result = dictService.getDictLabel("gender", "nonexistent");

        assertNull(result);
        verify(valueOperations).set("dict:gender:nonexistent", "NULL_SENTINEL", 5L, TimeUnit.MINUTES);
    }

    @Test
    @SuppressWarnings("unchecked")
    void clearDictCache_UsesScan_DeletesKeys() {
        Cursor<String> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn("dict:gender", "dict:status");
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);

        dictService.clearDictCache();

        verify(redisTemplate).scan(any(ScanOptions.class));
        verify(redisTemplate).delete(List.of("dict:gender", "dict:status"));
        verify(redisTemplate).delete("dict:all");
        verify(cursor).close();
    }
}
