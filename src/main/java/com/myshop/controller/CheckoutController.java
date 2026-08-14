package com.myshop.controller;

import com.myshop.domain.CartLine;
import com.myshop.service.CartService;
import com.myshop.service.OrderService;
import com.myshop.service.impl.CartServiceImpl;
import com.myshop.service.impl.OrderServiceImpl;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "checkoutController", urlPatterns = {"/account/checkout", "/account/orders"})
public class CheckoutController extends HttpServlet {
    private final OrderService orderService = new OrderServiceImpl();
    private final CartService cartService = new CartServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getServletPath().endsWith("orders")) {
            request.setAttribute("orders", orderService.getOrders(ControllerSupport.user(request).getId()));
            request.getRequestDispatcher("/WEB-INF/views/orders.jsp").forward(request, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/account/cart");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long userId = ControllerSupport.user(request).getId();
        try {
            String number = orderService.checkout(userId);
            request.setAttribute("orderNumber", number);
            request.getRequestDispatcher("/WEB-INF/views/order-complete.jsp").forward(request, response);
        } catch (IllegalStateException exception) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            List<CartLine> lines = cartService.get(userId);
            request.setAttribute("checkoutError", exception.getMessage());
            request.setAttribute("lines", lines);
            request.setAttribute("total", cartService.total(lines));
            request.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(request, response);
        } catch (RuntimeException exception) {
            getServletContext().log("Checkout failed", exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
