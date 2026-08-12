package com.myshop.controller;

import com.myshop.domain.CartLine;
import com.myshop.domain.SessionUser;
import com.myshop.service.CartService;
import com.myshop.service.impl.CartServiceImpl;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "cartController", urlPatterns = {"/account/cart", "/account/cart/api"})
public class CartController extends HttpServlet {
    private final CartService service = new CartServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long userId = user(request).getId();
        List<CartLine> lines = service.get(userId);
        if (request.getServletPath().endsWith("/api")) {
            json(response, true, service.count(lines), null);
            return;
        }
        request.setAttribute("lines", lines);
        request.setAttribute("total", service.total(lines));
        request.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            long userId = user(request).getId();
            long productId = Long.parseLong(request.getParameter("productId"));
            String action = request.getParameter("action");
            int quantity = Integer.parseInt(request.getParameter("quantity") == null ? "1" : request.getParameter("quantity"));
            if ("add".equals(action)) service.add(userId, productId, quantity);
            else if ("update".equals(action)) service.update(userId, productId, quantity);
            else if ("remove".equals(action)) service.remove(userId, productId);
            else throw new IllegalArgumentException("지원하지 않는 요청입니다.");
            json(response, true, service.count(service.get(userId)), null);
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json(response, false, 0, exception.getMessage());
        } catch (RuntimeException exception) {
            getServletContext().log("Cart operation failed", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json(response, false, 0, "장바구니 처리 중 문제가 발생했습니다.");
        }
    }

    private SessionUser user(HttpServletRequest request) {
        return (SessionUser) request.getSession().getAttribute("loginUser");
    }

    private void json(HttpServletResponse response, boolean success, int count, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":" + success + ",\"count\":" + count + ",\"message\":"
                + (message == null ? "null" : "\"" + escape(message) + "\"") + "}");
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
