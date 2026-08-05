package com.morrowshop.service;

import com.morrowshop.config.MyBatisProvider;
import com.morrowshop.domain.Product;
import com.morrowshop.mapper.ProductMapper;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

public class ProductService {
    public List<Product> getProducts() {
        return getProducts(null, null);
    }

    public List<Product> getProducts(String keyword, String category) {
        try (SqlSession session = MyBatisProvider.getFactory().openSession()) {
            return session.getMapper(ProductMapper.class).findActive(clean(keyword), clean(category));
        }
    }

    public Product getProduct(long id) {
        if (id <= 0) return null;
        try (SqlSession session = MyBatisProvider.getFactory().openSession()) {
            return session.getMapper(ProductMapper.class).findActiveById(id);
        }
    }

    public List<String> getCategories() {
        try (SqlSession session = MyBatisProvider.getFactory().openSession()) {
            return session.getMapper(ProductMapper.class).findCategories();
        }
    }

    private String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned.substring(0, Math.min(cleaned.length(), 100));
    }

    public List<Product> getAllAdmin() { try(SqlSession s=MyBatisProvider.getFactory().openSession()){return s.getMapper(ProductMapper.class).findAllAdmin();} }
    public Product getAdmin(long id) { try(SqlSession s=MyBatisProvider.getFactory().openSession()){return s.getMapper(ProductMapper.class).findByIdAdmin(id);} }
    public void save(Product product) { try(SqlSession s=MyBatisProvider.getFactory().openSession(false)){ProductMapper m=s.getMapper(ProductMapper.class); if(product.getId()==null)m.insert(product);else m.update(product);s.commit();} }
    public void delete(long id) { try(SqlSession s=MyBatisProvider.getFactory().openSession(false)){s.getMapper(ProductMapper.class).softDelete(id);s.commit();} }
}
