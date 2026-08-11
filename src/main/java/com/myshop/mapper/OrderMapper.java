package com.myshop.mapper;
import com.myshop.domain.OrderSummary;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
public interface OrderMapper {
 int insertOrder(@Param("userId") long userId,@Param("orderNumber") String number,@Param("total") BigDecimal total);
 Long findIdByNumber(@Param("orderNumber") String number);
 int insertItem(@Param("orderId") long orderId,@Param("line") com.myshop.domain.CartLine line);
 int decreaseStock(@Param("productId") long productId,@Param("quantity") int quantity);
 List<OrderSummary> findByUser(@Param("userId") long userId);
}
