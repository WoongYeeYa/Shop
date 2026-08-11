package com.myshop.mapper;
import com.myshop.domain.CartLine;
import java.util.List;
import org.apache.ibatis.annotations.Param;
public interface CartMapper {
 List<CartLine> findByUser(@Param("userId") long userId);
 List<CartLine> findByUserForUpdate(@Param("userId") long userId);
 int upsert(@Param("userId") long userId,@Param("productId") long productId,@Param("quantity") int quantity);
 int update(@Param("userId") long userId,@Param("productId") long productId,@Param("quantity") int quantity);
 int delete(@Param("userId") long userId,@Param("productId") long productId);
 int clear(@Param("userId") long userId);
}
