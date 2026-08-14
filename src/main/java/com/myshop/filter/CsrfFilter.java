package com.myshop.filter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

@WebFilter(filterName = "csrfFilter", urlPatterns = "/*")
public class CsrfFilter implements Filter {
    private static final String TOKEN = "csrfToken";
    private final SecureRandom random = new SecureRandom();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if (request.getRequestURI().startsWith(request.getContextPath() + "/assets/")) {
            chain.doFilter(request, response);
            return;
        }
        HttpSession session = request.getSession();
        if (session.getAttribute(TOKEN) == null) session.setAttribute(TOKEN, createToken());
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String expected = (String) session.getAttribute(TOKEN);
            String actual = request.getParameter(TOKEN);
            if (actual == null) actual = request.getHeader("X-CSRF-Token");
            if (!constantTimeEquals(expected, actual)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private String createToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    private boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null || left.length() != right.length()) return false;
        int result = 0;
        for (int i = 0; i < left.length(); i++) result |= left.charAt(i) ^ right.charAt(i);
        return result == 0;
    }
}
