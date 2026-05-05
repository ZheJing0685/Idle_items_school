package com.idleitems.school.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import java.io.IOException;
import java.util.regex.Pattern;

@Slf4j
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
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String contentType = httpRequest.getContentType();
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            chain.doFilter(request, response);
        } else {
            XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(httpRequest);
            chain.doFilter(xssRequest, response);
        }
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
