package com.myshop.filter;

import com.myshop.domain.SessionUser;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

@WebFilter(filterName = "authFilter", urlPatterns = {"/account/*", "/admin/*"})
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginUser") == null) {
            HttpSession targetSession = request.getSession();
            String target = request.getRequestURI();
            if (request.getQueryString() != null) target += "?" + request.getQueryString();
            targetSession.setAttribute("redirectAfterLogin", target);
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        SessionUser user = (SessionUser) session.getAttribute("loginUser");
        if (request.getRequestURI().startsWith(request.getContextPath() + "/admin/") && !user.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }
}
