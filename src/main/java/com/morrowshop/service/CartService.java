package com.morrowshop.service;

import com.morrowshop.domain.CartLine;
import java.math.BigDecimal;
import java.util.List;

public interface CartService {
    List<CartLine> get(long userId);
    void add(long userId, long productId, int quantity);
    void update(long userId, long productId, int quantity);
    void remove(long userId, long productId);
    BigDecimal total(List<CartLine> lines);
    int count(List<CartLine> lines);
}
