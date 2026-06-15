package com.idleitems.school.security.filter;

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

    /**
     * 危险的XSS模式 - 只过滤真正危险的内容
     * 不再移除所有HTML标签，允许安全的富文本
     */
    private static final Pattern[] XSS_PATTERNS = {
        Pattern.compile("<script[^>]*>(.*?)</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("on(?:click|dblclick|load|error|mouseover|mouseout|mousedown|mouseup|mousemove|keydown|keyup|keypress|focus|blur|change|submit|reset|select|input)\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("expression\\s*\\([^)]*\\)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("data:\\s*text/html", Pattern.CASE_INSENSITIVE),
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<iframe[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("<object[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("<embed[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("<form[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("alert\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("prompt\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("confirm\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("document\\.cookie", Pattern.CASE_INSENSITIVE),
        Pattern.compile("document\\.write", Pattern.CASE_INSENSITIVE),
        Pattern.compile("window\\.location", Pattern.CASE_INSENSITIVE)
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("XSS过滤器初始化完成（安全模式：仅过滤危险模式）");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String method = httpRequest.getMethod();
        String contentType = httpRequest.getContentType();

        if ("GET".equals(method)) {
            XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(httpRequest, false);
            chain.doFilter(xssRequest, response);
        } else if (("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method))
                && (contentType == null || !contentType.startsWith("multipart/form-data"))) {
            XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(httpRequest, true);
            chain.doFilter(xssRequest, response);
        } else {
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

    /**
     * 过滤XSS内容
     * 只移除危险的XSS模式，保留安全的HTML标签
     */
    public static String filterXss(String input) {
        if (input == null) {
            return null;
        }

        String filtered = input;

        for (Pattern pattern : XSS_PATTERNS) {
            filtered = pattern.matcher(filtered).replaceAll("");
        }

        return filtered;
    }
}
