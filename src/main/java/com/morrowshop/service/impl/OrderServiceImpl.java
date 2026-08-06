package com.morrowshop.service.impl;

import com.morrowshop.config.MyBatisProvider;
import com.morrowshop.domain.CartLine;
import com.morrowshop.domain.OrderSummary;
import com.morrowshop.mapper.CartMapper;
import com.morrowshop.mapper.OrderMapper;
import com.morrowshop.service.OrderService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.apache.ibatis.session.SqlSession;

public class OrderServiceImpl implements OrderService {
    @Override
    public String checkout(long userId) {
        try (SqlSession session = MyBatisProvider.getFactory().openSession(false)) {
            CartMapper carts = session.getMapper(CartMapper.class);
            OrderMapper orders = session.getMapper(OrderMapper.class);
            List<CartLine> lines = carts.findByUserForUpdate(userId);
            if (lines.isEmpty()) throw new IllegalStateException("장바구니가 비어 있습니다.");
            BigDecimal total = BigDecimal.ZERO;
            for (CartLine line : lines) {
                if (line.getQuantity() > line.getStock()) throw new IllegalStateException(line.getName() + "의 재고가 부족합니다.");
                total = total.add(line.getSubtotal());
            }
            String number = "MRW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            orders.insertOrder(userId, number, total);
            Long orderId = orders.findIdByNumber(number);
            for (CartLine line : lines) {
                if (orders.decreaseStock(line.getProductId(), line.getQuantity()) != 1) throw new IllegalStateException("결제 중 재고가 변경되었습니다.");
                orders.insertItem(orderId, line);
            }
            carts.clear(userId);
            session.commit();
            return number;
        }
    }

    @Override
    public List<OrderSummary> getOrders(long userId) {
        try (SqlSession session = MyBatisProvider.getFactory().openSession()) {
            return session.getMapper(OrderMapper.class).findByUser(userId);
        }
    }
}
