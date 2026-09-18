package com.myshop.mapper;

import com.myshop.domain.*;
import java.sql.Timestamp;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SupportMapper {
    Long lockUser(long id);
    int countRecent(@Param("userId") long userId, @Param("since") Timestamp since);
    int insertInquiry(Inquiry inquiry);
    Inquiry find(@Param("id") long id, @Param("userId") Long userId);
    List<Inquiry> list(@Param("userId") Long userId, @Param("status") String status, @Param("offset") int offset);
    OrderSummary ownedOrder(@Param("id") long id, @Param("userId") long userId);
    int orderHasProduct(@Param("orderId") long orderId, @Param("productId") long productId);
    String orderItems(long orderId);
    int publish(@Param("id") long id, @Param("adminId") long adminId, @Param("answer") String answer, @Param("version") int version);
    int claim(@Param("id") long id, @Param("token") String token, @Param("cooldown") Timestamp cooldown, @Param("expired") Timestamp expired);
    int finish(@Param("id") long id, @Param("token") String token, @Param("run") AiRun run);
    int failClaim(@Param("id") long id, @Param("token") String token);
    int runCount(long id);
    int insertRun(AiRun run);
    List<AiRun> runs(long id);
    List<SupportPolicy> policies(@Param("activeOnly") boolean activeOnly);
    int insertPolicy(SupportPolicy policy);
    int updatePolicy(SupportPolicy policy);
}
