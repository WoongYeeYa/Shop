package com.myshop.service;

import com.myshop.domain.OrderSummary;
import java.util.List;

public interface OrderService {
    String checkout(long userId);
    List<OrderSummary> getOrders(long userId);
}
