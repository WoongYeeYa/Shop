package com.myshop.service.impl;

import com.myshop.config.MyBatisProvider;
import com.myshop.domain.Product;
import com.myshop.mapper.ProductMapper;
import com.myshop.service.ProductService;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

public class ProductServiceImpl implements ProductService {
    @Override public List<Product> getProducts(String keyword, String category) { try (SqlSession s = MyBatisProvider.getFactory().openSession()) { return s.getMapper(ProductMapper.class).findActive(clean(keyword), clean(category)); } }
    @Override public Product getProduct(long id) { if (id <= 0) return null; try (SqlSession s = MyBatisProvider.getFactory().openSession()) { return s.getMapper(ProductMapper.class).findActiveById(id); } }
    @Override public List<String> getCategories() { try (SqlSession s = MyBatisProvider.getFactory().openSession()) { return s.getMapper(ProductMapper.class).findCategories(); } }
    @Override public List<Product> getAllAdmin() { try (SqlSession s = MyBatisProvider.getFactory().openSession()) { return s.getMapper(ProductMapper.class).findAllAdmin(); } }
    @Override public Product getAdmin(long id) { try (SqlSession s = MyBatisProvider.getFactory().openSession()) { return s.getMapper(ProductMapper.class).findByIdAdmin(id); } }
    @Override
    public void save(Product product) {
        try (SqlSession session = MyBatisProvider.getFactory().openSession(false)) {
            ProductMapper mapper = session.getMapper(ProductMapper.class);
            int changed = product.getId() == null ? mapper.insert(product) : mapper.update(product);
            if (changed != 1) throw new IllegalArgumentException("존재하지 않는 상품입니다.");
            session.commit();
        }
    }

    @Override
    public void delete(long id) {
        try (SqlSession session = MyBatisProvider.getFactory().openSession(false)) {
            if (session.getMapper(ProductMapper.class).softDelete(id) != 1) {
                throw new IllegalArgumentException("존재하지 않는 상품입니다.");
            }
            session.commit();
        }
    }

    private String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned.substring(0, Math.min(cleaned.length(), 100));
    }
}
