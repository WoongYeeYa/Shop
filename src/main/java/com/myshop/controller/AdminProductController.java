package com.myshop.controller;

import com.myshop.domain.Product;
import com.myshop.service.ProductService;
import com.myshop.service.impl.ProductServiceImpl;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "adminProductController", urlPatterns = {"/admin/products", "/admin/product"})
public class AdminProductController extends HttpServlet {
    private final ProductService service = new ProductServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/admin/product".equals(request.getServletPath())) {
            String rawId = request.getParameter("id");
            if (rawId != null && !rawId.isEmpty()) {
                try {
                    Product product = service.getAdmin(Long.parseLong(rawId));
                    if (product == null) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                    }
                    request.setAttribute("product", product);
                } catch (NumberFormatException exception) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            }
            request.getRequestDispatcher("/WEB-INF/views/admin/product-form.jsp").forward(request, response);
            return;
        }
        request.setAttribute("products", service.getAllAdmin());
        request.getRequestDispatcher("/WEB-INF/views/admin/products.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            if ("delete".equals(request.getParameter("action"))) {
                service.delete(Long.parseLong(request.getParameter("id")));
            } else {
                service.save(read(request));
            }
            response.sendRedirect(request.getContextPath() + "/admin/products");
        } catch (IllegalArgumentException exception) {
            if ("delete".equals(request.getParameter("action"))) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
                return;
            }
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("error", exception.getMessage());
            request.setAttribute("product", readSafely(request));
            request.getRequestDispatcher("/WEB-INF/views/admin/product-form.jsp").forward(request, response);
        } catch (RuntimeException exception) {
            getServletContext().log("Admin product operation failed", exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private Product read(HttpServletRequest request) {
        Product product = new Product();
        String id = request.getParameter("id");
        if (id != null && !id.isEmpty()) product.setId(Long.valueOf(id));
        product.setSku(required(request, "sku", 40));
        product.setName(required(request, "name", 120));
        product.setDescription(required(request, "description", 5000));
        product.setCategory(required(request, "category", 50));
        product.setImageUrl(validImageUrl(required(request, "imageUrl", 500)));
        try {
            product.setPrice(new BigDecimal(request.getParameter("price")));
            product.setStock(Integer.parseInt(request.getParameter("stock")));
        } catch (NumberFormatException | NullPointerException exception) {
            throw new IllegalArgumentException("가격과 재고를 올바르게 입력해 주세요.");
        }
        if (product.getPrice().signum() < 0 || product.getStock() < 0) {
            throw new IllegalArgumentException("가격과 재고는 0 이상이어야 합니다.");
        }
        product.setFeatured("true".equals(request.getParameter("featured")));
        product.setActive(true);
        return product;
    }

    private Product readSafely(HttpServletRequest request) {
        Product product = new Product();
        try {
            product.setId(Long.valueOf(ControllerSupport.rawValue(request, "id")));
        } catch (NumberFormatException ignored) {
            // Leave a missing or malformed id unset so the form can still render.
        }
        product.setSku(ControllerSupport.value(request, "sku"));
        product.setName(ControllerSupport.value(request, "name"));
        product.setDescription(ControllerSupport.value(request, "description"));
        product.setCategory(ControllerSupport.value(request, "category"));
        product.setImageUrl(ControllerSupport.value(request, "imageUrl"));
        try {
            product.setPrice(new BigDecimal(ControllerSupport.rawValue(request, "price")));
        } catch (NumberFormatException ignored) {
            // Invalid values remain empty/default and are explained by the form error.
        }
        try {
            product.setStock(Integer.parseInt(ControllerSupport.rawValue(request, "stock")));
        } catch (NumberFormatException ignored) {
            // Invalid values remain empty/default and are explained by the form error.
        }
        product.setFeatured("true".equals(request.getParameter("featured")));
        return product;
    }

    private String required(HttpServletRequest request, String name, int maxLength) {
        String value = ControllerSupport.value(request, name);
        if (value.isEmpty() || value.length() > maxLength) {
            throw new IllegalArgumentException("필수 항목과 입력 길이를 확인해 주세요.");
        }
        return value;
    }

    private String validImageUrl(String value) {
        try {
            String scheme = URI.create(value).getScheme();
            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) return value;
        } catch (IllegalArgumentException ignored) {
            // Use the same validation message for malformed and unsupported URLs.
        }
        throw new IllegalArgumentException("이미지 주소는 http 또는 https URL이어야 합니다.");
    }
}
