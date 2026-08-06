package com.morrowshop.service;

import com.morrowshop.domain.Product;
import java.util.List;

public interface ProductService {
    List<Product> getProducts();
    List<Product> getProducts(String keyword, String category);
    Product getProduct(long id);
    List<String> getCategories();
    List<Product> getAllAdmin();
    Product getAdmin(long id);
    void save(Product product);
    void delete(long id);
}
