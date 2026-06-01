package com.idleitems.school.security.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import java.util.*;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private byte[] body;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, String[]> filteredParameterMap;

    public XssHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        this(request, true);
    }

    /**
     * @param request       原始请求
     * @param readBody      是否读取并过滤请求体（POST/PUT/PATCH 为 true，GET 为 false）
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request, boolean readBody) throws IOException {
        super(request);
        if (readBody) {
            String bodyString = getBodyString(request);
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                body = filterJsonXss(bodyString);
            } else {
                body = XssFilter.filterXss(bodyString).getBytes(StandardCharsets.UTF_8);
            }
        } else {
            body = null;
        }
        // 预过滤查询参数
        filterParameters();
    }

    private void filterParameters() {
        Map<String, String[]> originalMap = super.getParameterMap();
        filteredParameterMap = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : originalMap.entrySet()) {
            String key = XssFilter.filterXss(entry.getKey());
            String[] values = entry.getValue();
            String[] filteredValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                filteredValues[i] = XssFilter.filterXss(values[i]);
            }
            filteredParameterMap.put(key, filteredValues);
        }
    }

    @Override
    public String getParameter(String name) {
        String[] values = filteredParameterMap.get(name);
        return (values != null && values.length > 0) ? values[0] : null;
    }

    @Override
    public String[] getParameterValues(String name) {
        return filteredParameterMap.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(filteredParameterMap);
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

    private byte[] filterJsonXss(String jsonString) throws IOException {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            JsonNode filteredNode = filterJsonNode(rootNode);
            return objectMapper.writeValueAsString(filteredNode).getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            // JSON 解析失败，拒绝请求而非静默放行
            throw new IOException("请求体JSON格式错误，无法进行安全过滤", e);
        }
    }

    private JsonNode filterJsonNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            ObjectNode filteredObject = objectMapper.createObjectNode();
            node.properties().forEach(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                filteredObject.set(key, filterJsonNode(value));
            });
            return filteredObject;
        } else if (node.isArray()) {
            // 修复：递归处理数组中的每个元素
            ArrayNode arrayNode = (ArrayNode) node;
            ArrayNode filteredArray = objectMapper.createArrayNode();
            for (int i = 0; i < arrayNode.size(); i++) {
                filteredArray.add(filterJsonNode(arrayNode.get(i)));
            }
            return filteredArray;
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
