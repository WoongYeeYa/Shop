package com.morrowshop.service;

import com.morrowshop.domain.OrderSummary;
import java.util.List;

public interface OrderService {
    String checkout(long userId);
    List<OrderSummary> getOrders(long userId);
}
