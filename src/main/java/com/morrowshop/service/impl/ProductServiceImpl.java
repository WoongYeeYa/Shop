package com.morrowshop.service.impl;

import com.morrowshop.config.MyBatisProvider;
import com.morrowshop.domain.Product;
import com.morrowshop.mapper.ProductMapper;
import com.morrowshop.service.ProductService;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

public class ProductServiceImpl implements ProductService {
    @Override public List<Product> getProducts() { return getProducts(null, null); }
    @Override public List<Product> getProducts(String keyword, String category) { try (SqlSession s = MyBatisProvider.getFactory().openSession()) { return s.getMapper(ProductMapper.class).findActive(clean(keyword), clean(category)); } }
    @Override public Product getProduct(long id) { if (id <= 0) return null; try (SqlSession s = MyBatisProvider.getFactory().openSession()) { return s.getMapper(ProductMapper.class).findActiveById(id); } }
    @Override public List<String> getCategories() { try (SqlSession s = MyBatisProvider.getFactory().openSession()) { return s.getMapper(ProductMapper.class).findCategories(); } }
    @Override public List<Product> getAllAdmin() { try (SqlSession s = MyBatisProvider.getFactory().openSession()) { return s.getMapper(ProductMapper.class).findAllAdmin(); } }
    @Override public Product getAdmin(long id) { try (SqlSession s = MyBatisProvider.getFactory().openSession()) { return s.getMapper(ProductMapper.class).findByIdAdmin(id); } }
    @Override public void save(Product product) { try (SqlSession s = MyBatisProvider.getFactory().openSession(false)) { ProductMapper mapper = s.getMapper(ProductMapper.class); if (product.getId() == null) mapper.insert(product); else mapper.update(product); s.commit(); } }
    @Override public void delete(long id) { try (SqlSession s = MyBatisProvider.getFactory().openSession(false)) { s.getMapper(ProductMapper.class).softDelete(id); s.commit(); } }

    private String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned.substring(0, Math.min(cleaned.length(), 100));
    }
}
