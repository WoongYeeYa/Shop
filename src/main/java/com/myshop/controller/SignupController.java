package com.myshop.controller;

import com.myshop.domain.SessionUser;
import com.myshop.service.AuthService;
import com.myshop.service.impl.AuthServiceImpl;
import com.myshop.service.DuplicateEmailException;
import java.io.IOException;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name = "signupController", urlPatterns = "/signup")
public class SignupController extends HttpServlet {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/auth/signup.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = ControllerSupport.value(request, "name");
        String email = ControllerSupport.value(request, "email");
        String password = ControllerSupport.rawValue(request, "password");
        String passwordConfirm = ControllerSupport.rawValue(request, "passwordConfirm");
        request.setAttribute("name", name);
        request.setAttribute("email", email);
        String error = validate(name, email, password, passwordConfirm);
        if (error != null) { showError(request, response, error); return; }
        try {
            SessionUser user = authService.register(email, password, name);
            HttpSession session = request.getSession();
            request.changeSessionId();
            session.removeAttribute("csrfToken");
            session.setAttribute("loginUser", user);
            response.sendRedirect(request.getContextPath() + "/products");
        } catch (DuplicateEmailException exception) {
            showError(request, response, exception.getMessage());
        } catch (RuntimeException exception) {
            getServletContext().log("Signup failed", exception);
            showError(request, response, "가입 처리 중 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    private String validate(String name, String email, String password, String confirm) {
        if (name.length() < 2 || name.length() > 50) return "이름은 2자 이상 50자 이하로 입력해 주세요.";
        if (email.length() > 190 || !EMAIL.matcher(email).matches()) return "올바른 이메일 주소를 입력해 주세요.";
        if (password.length() < 8 || password.length() > 72) return "비밀번호는 8자 이상 72자 이하로 입력해 주세요.";
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*[0-9].*")) return "비밀번호에 영문과 숫자를 모두 포함해 주세요.";
        if (!password.equals(confirm)) return "비밀번호 확인이 일치하지 않습니다.";
        return null;
    }
    private void showError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        request.setAttribute("error", message);
        request.getRequestDispatcher("/WEB-INF/views/auth/signup.jsp").forward(request, response);
    }
}
