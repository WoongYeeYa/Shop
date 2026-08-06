package com.morrowshop.controller;

import com.morrowshop.service.ProductService;
import com.morrowshop.service.impl.ProductServiceImpl;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "homeController", urlPatterns = {"", "/products"})
public class HomeController extends HttpServlet {
    private final ProductService productService = new ProductServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        String category = request.getParameter("category");
        request.setAttribute("keyword", keyword == null ? "" : keyword.trim());
        request.setAttribute("selectedCategory", category == null ? "" : category.trim());
        try {
            request.setAttribute("products", productService.getProducts(keyword, category));
            request.setAttribute("categories", productService.getCategories());
        } catch (RuntimeException exception) {
            getServletContext().log("Product catalog query failed", exception);
            request.setAttribute("catalogError", "상품을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.");
        }
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
