package com.idleitems.school;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idleitems.school.config.TestRedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 集成测试基类
 * 提供通用的测试配置和工具方法
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "JWT_SECRET=dGhpcy1pcy1hLTI1Ni1iaXQtc2VjcmV0LWtleS1mb3ItdGVzdGluZy1wdXJwb3Nlcy1vbmx5",
    "ENCRYPTION_SECRET_KEY=test-encryption-secret-key-12345678"
})
@Import(TestRedisConfig.class)
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * 将对象转换为JSON字符串
     */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * 构建Bearer Token头
     */
    protected String bearerToken(String token) {
        return "Bearer " + token;
    }
}
