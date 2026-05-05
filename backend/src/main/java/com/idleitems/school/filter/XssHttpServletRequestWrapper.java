package com.idleitems.school.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public XssHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        String bodyString = getBodyString(request);
        String contentType = request.getContentType();
        
        if (contentType != null && contentType.contains("application/json")) {
            // 处理JSON请求体
            body = filterJsonXss(bodyString).getBytes(StandardCharsets.UTF_8);
        } else {
            // 处理普通请求体
            body = XssFilter.filterXss(bodyString).getBytes(StandardCharsets.UTF_8);
        }
    }

    private String getBodyString(HttpServletRequest request) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            char[] chars = new char[128];
            int len;
            while ((len = bufferedReader.read(chars)) > 0) {
                stringBuilder.append(chars, 0, len);
            }
        }
        return stringBuilder.toString();
    }

    private String filterJsonXss(String jsonString) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            JsonNode filteredNode = filterJsonNode(rootNode);
            return objectMapper.writeValueAsString(filteredNode);
        } catch (Exception e) {
            // 如果解析失败，直接返回原始JSON字符串
            // 不要使用XssFilter.filterXss()，因为它会破坏JSON格式
            return jsonString;
        }
    }

    private JsonNode filterJsonNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            ObjectNode filteredObject = objectMapper.createObjectNode();
            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                filteredObject.set(key, filterJsonNode(value));
            });
            return filteredObject;
        } else if (node.isArray()) {
            return node; // 数组暂时不处理
        } else if (node.isTextual()) {
            return objectMapper.valueToTree(XssFilter.filterXss(node.asText()));
        } else {
            return node; // 其他类型保持不变
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }
}
