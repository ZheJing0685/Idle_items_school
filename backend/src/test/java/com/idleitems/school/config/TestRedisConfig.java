package com.idleitems.school.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.idleitems.school.security.JwtTokenBlacklistService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestRedisConfig {

    private final ConcurrentMap<String, byte[]> store = new ConcurrentHashMap<>();

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        RedisConnection connection = mock(RedisConnection.class);
        RedisKeyCommands keyCommands = mock(RedisKeyCommands.class);
        RedisStringCommands stringCommands = mock(RedisStringCommands.class);
        RedisScriptingCommands scriptingCommands = mock(RedisScriptingCommands.class);

        when(factory.getConnection()).thenReturn(connection);
        when(factory.getSentinelConnection()).thenReturn(null);

        when(connection.isClosed()).thenReturn(false);
        when(connection.getNativeConnection()).thenReturn(null);
        when(connection.keyCommands()).thenReturn(keyCommands);
        when(connection.stringCommands()).thenReturn(stringCommands);
        when(connection.scriptingCommands()).thenReturn(scriptingCommands);

        // In-memory store for keyCommands
        when(keyCommands.del(any())).thenAnswer(invocation -> {
            byte[] key = invocation.getArgument(0);
            return store.remove(keyToStr(key)) != null ? 1L : 0L;
        });
        when(keyCommands.exists(any(byte[].class))).thenAnswer(invocation -> {
            return store.containsKey(keyToStr(invocation.getArgument(0)));
        });
        when(keyCommands.ttl(any())).thenReturn(0L);
        when(keyCommands.expire(any(), anyLong())).thenReturn(true);

        // In-memory store for stringCommands
        when(stringCommands.get(any())).thenAnswer(invocation -> {
            return store.get(keyToStr(invocation.getArgument(0)));
        });
        doAnswer(invocation -> {
            byte[] key = invocation.getArgument(0);
            byte[] value = invocation.getArgument(1);
            store.put(keyToStr(key), value);
            return "OK";
        }).when(stringCommands).set(any(), any());
        doAnswer(invocation -> {
            byte[] key = invocation.getArgument(0);
            byte[] value = invocation.getArgument(2);
            store.put(keyToStr(key), value);
            return "OK";
        }).when(stringCommands).setEx(any(), anyLong(), any());
        doAnswer(invocation -> {
            byte[] key = invocation.getArgument(0);
            byte[] value = invocation.getArgument(2);
            store.put(keyToStr(key), value);
            return Boolean.TRUE;
        }).when(stringCommands).pSetEx(any(), anyLong(), any());
        when(stringCommands.incr(any())).thenAnswer(invocation -> {
            String key = keyToStr(invocation.getArgument(0));
            long newVal = 1L;
            byte[] existing = store.get(key);
            if (existing != null) {
                newVal = Long.parseLong(new String(existing, java.nio.charset.StandardCharsets.UTF_8)) + 1;
            }
            store.put(key, String.valueOf(newVal).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return newVal;
        });
        when(stringCommands.mGet(any())).thenReturn(null);
        when(stringCommands.append(any(), any())).thenReturn(0L);

        // Rate limit script returns 1 (not rate limited)
        when(scriptingCommands.evalSha(anyString(), any(), anyInt(), any())).thenReturn(1L);
        when(scriptingCommands.eval(any(), any(), anyInt(), any())).thenReturn(1L);

        doNothing().when(connection).close();

        return factory;
    }

    private String keyToStr(byte[] key) {
        return new String(key, java.nio.charset.StandardCharsets.UTF_8);
    }

    private ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @Primary
    public JwtTokenBlacklistService jwtTokenBlacklistService() {
        Set<String> blacklist = new HashSet<>();
        JwtTokenBlacklistService mockService = mock(JwtTokenBlacklistService.class);
        doAnswer(invocation -> { blacklist.add(invocation.getArgument(0)); return null; })
                .when(mockService).addToBlacklist(anyString(), anyLong());
        when(mockService.isBlacklisted(anyString())).thenAnswer(invocation ->
                blacklist.contains(invocation.getArgument(0)));
        return mockService;
    }

    @Bean
    public DefaultRedisScript<Long> rateLimitScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText("return 1");
        script.setResultType(Long.class);
        return script;
    }
}
