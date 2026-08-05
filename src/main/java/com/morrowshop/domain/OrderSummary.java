package com.morrowshop.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class OrderSummary {
    private Long id; private String orderNumber; private String status; private BigDecimal totalAmount; private Timestamp createdAt;
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getOrderNumber(){return orderNumber;} public void setOrderNumber(String v){orderNumber=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal v){totalAmount=v;}
    public Timestamp getCreatedAt(){return createdAt;} public void setCreatedAt(Timestamp v){createdAt=v;}
}
