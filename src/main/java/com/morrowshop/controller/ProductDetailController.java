package com.morrowshop.controller;

import com.morrowshop.domain.Product;
import com.morrowshop.service.ProductService;
import com.morrowshop.service.impl.ProductServiceImpl;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "productDetailController", urlPatterns = "/product")
public class ProductDetailController extends HttpServlet {
    private final ProductService productService = new ProductServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long id;
        try {
            id = Long.parseLong(request.getParameter("id"));
        } catch (NumberFormatException | NullPointerException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "올바른 상품 번호가 필요합니다.");
            return;
        }
        try {
            Product product = productService.getProduct(id);
            if (product == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("product", product);
            request.getRequestDispatcher("/WEB-INF/views/product-detail.jsp").forward(request, response);
        } catch (RuntimeException exception) {
            getServletContext().log("Product detail query failed: " + id, exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
