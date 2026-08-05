package com.morrowshop.mapper;

import com.morrowshop.domain.Product;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ProductMapper {
    List<Product> findActive(@Param("keyword") String keyword, @Param("category") String category);
    Product findActiveById(@Param("id") long id);
    List<String> findCategories();
    List<Product> findAllAdmin();
    Product findByIdAdmin(@Param("id") long id);
    int insert(Product product);
    int update(Product product);
    int softDelete(@Param("id") long id);
}
