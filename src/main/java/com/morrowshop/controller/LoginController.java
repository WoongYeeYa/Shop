package com.morrowshop.controller;

import com.morrowshop.domain.SessionUser;
import com.morrowshop.service.AuthService;
import com.morrowshop.service.impl.AuthServiceImpl;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "loginController", urlPatterns = "/login")
public class LoginController extends HttpServlet {
    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getSession(false) != null && request.getSession(false).getAttribute("loginUser") != null) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = value(request, "email");
        String password = rawValue(request, "password");
        request.setAttribute("email", email);
        if (email.isEmpty() || password.isEmpty()) {
            showError(request, response, "이메일과 비밀번호를 모두 입력해 주세요.");
            return;
        }
        try {
            SessionUser user = authService.authenticate(email, password);
            if (user == null) {
                showError(request, response, "이메일 또는 비밀번호가 올바르지 않습니다.");
                return;
            }

            
            HttpSession session = request.getSession();
            request.changeSessionId();
            session.removeAttribute("csrfToken");
            session.setAttribute("loginUser", user);
            String redirect = (String) session.getAttribute("redirectAfterLogin");
            session.removeAttribute("redirectAfterLogin");
            response.sendRedirect(redirect != null && redirect.startsWith(request.getContextPath() + "/")
                    ? redirect : request.getContextPath() + "/products");
        } catch (RuntimeException exception) {
            getServletContext().log("Login failed", exception);
            showError(request, response, "로그인 처리 중 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    private void showError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        request.setAttribute("error", message);
        request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
    }
    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }
    private String rawValue(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value;
    }
}
