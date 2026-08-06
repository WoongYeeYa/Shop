package com.morrowshop.service.impl;

import com.morrowshop.config.MyBatisProvider;
import com.morrowshop.domain.CartLine;
import com.morrowshop.domain.Product;
import com.morrowshop.mapper.CartMapper;
import com.morrowshop.mapper.ProductMapper;
import com.morrowshop.service.CartService;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

public class CartServiceImpl implements CartService {
    @Override
    public List<CartLine> get(long userId) {
        try (SqlSession session = MyBatisProvider.getFactory().openSession()) {
            return session.getMapper(CartMapper.class).findByUser(userId);
        }
    }

    @Override
    public void add(long userId, long productId, int quantity) {
        if (quantity < 1 || quantity > 99) throw new IllegalArgumentException("수량은 1개 이상 99개 이하여야 합니다.");
        try (SqlSession session = MyBatisProvider.getFactory().openSession(false)) {
            CartMapper cart = session.getMapper(CartMapper.class);
            List<CartLine> lines = cart.findByUserForUpdate(userId);
            Product product = session.getMapper(ProductMapper.class).findActiveById(productId);
            int current = 0;
            for (CartLine line : lines) if (line.getProductId() == productId) current = line.getQuantity();
            if (product == null || current + quantity > product.getStock() || current + quantity > 99) {
                throw new IllegalArgumentException("상품 재고가 부족합니다.");
            }
            cart.upsert(userId, productId, quantity);
            session.commit();
        }
    }

    @Override
    public void update(long userId, long productId, int quantity) {
        if (quantity < 1) { remove(userId, productId); return; }
        try (SqlSession session = MyBatisProvider.getFactory().openSession(false)) {
            CartMapper cart = session.getMapper(CartMapper.class);
            cart.findByUserForUpdate(userId);
            Product product = session.getMapper(ProductMapper.class).findActiveById(productId);
            if (product == null || product.getStock() < quantity || quantity > 99) {
                throw new IllegalArgumentException("선택한 수량만큼 재고가 없습니다.");
            }
            cart.update(userId, productId, quantity);
            session.commit();
        }
    }

    @Override public void remove(long userId, long productId) { try (SqlSession s = MyBatisProvider.getFactory().openSession(false)) { s.getMapper(CartMapper.class).delete(userId, productId); s.commit(); } }
    @Override public BigDecimal total(List<CartLine> lines) { BigDecimal total = BigDecimal.ZERO; for (CartLine line : lines) total = total.add(line.getSubtotal()); return total; }
    @Override public int count(List<CartLine> lines) { int count = 0; for (CartLine line : lines) count += line.getQuantity(); return count; }
}
