package com.myshop.domain;

import java.math.BigDecimal;

public class CartLine {
    private Long productId; private String name; private BigDecimal price; private int quantity; private int stock; private String imageUrl;
    public Long getProductId(){return productId;} public void setProductId(Long v){productId=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal v){price=v;}
    public int getQuantity(){return quantity;} public void setQuantity(int v){quantity=v;}
    public int getStock(){return stock;} public void setStock(int v){stock=v;}
    public String getImageUrl(){return imageUrl;} public void setImageUrl(String v){imageUrl=v;}
    public BigDecimal getSubtotal(){return price.multiply(BigDecimal.valueOf(quantity));}
}
