package com.idleitems.school.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.regex.Pattern;

@Slf4j
@Component
public class XssFilter implements Filter {

    private static final Pattern[] XSS_PATTERNS = {
        Pattern.compile("<script[^>]*>(.*?)</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<[^>]*>"),
        Pattern.compile("expression\\s*\\([^)]*\\)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("data:\\s*text/html", Pattern.CASE_INSENSITIVE),
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<iframe[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("alert\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("prompt\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("confirm\\s*\\(", Pattern.CASE_INSENSITIVE)
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("XSS过滤器初始化完成");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            String method = httpRequest.getMethod();
            String contentType = httpRequest.getContentType();
            
            // 只对有请求体的方法（POST/PUT/PATCH）且非multipart进行XSS包装
            if (("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method))
                    && (contentType == null || !contentType.startsWith("multipart/form-data"))) {
                XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(httpRequest);
                chain.doFilter(xssRequest, response);
            } else {
                chain.doFilter(request, response);
            }
        } catch (IOException e) {
            log.error("XSS过滤器处理请求体时发生IO异常: {}", e.getMessage(), e);
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"code\":40001,\"message\":\"请求体读取异常\",\"data\":null}");
        } catch (Exception e) {
            log.error("XSS过滤器处理请求时发生异常: {}", e.getMessage(), e);
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        log.info("XSS过滤器已销毁");
    }

    public static String escapeHtml(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forHtml(input);
    }

    public static String escapeHtmlAttribute(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forHtmlAttribute(input);
    }

    public static String escapeJavaScript(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forJavaScript(input);
    }

    public static String filterXss(String input) {
        if (input == null) {
            return null;
        }

        String filtered = input;

        for (Pattern pattern : XSS_PATTERNS) {
            filtered = pattern.matcher(filtered).replaceAll("");
        }

        filtered = Encode.forHtml(filtered);

        return filtered;
    }
}
