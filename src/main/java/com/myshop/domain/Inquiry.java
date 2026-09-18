package com.myshop.domain;

import java.sql.Timestamp;

public class Inquiry {
    private Long id, userId, productId, orderId, answeredBy;
    private String category, title, body, status, answer, aiState, aiToken, draft, reviewReason;
    private String productName, orderNumber;
    private int version;
    private boolean needsReview;
    private Timestamp createdAt, answeredAt, aiStartedAt;
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;}
    public Long getProductId(){return productId;} public void setProductId(Long v){productId=v;}
    public Long getOrderId(){return orderId;} public void setOrderId(Long v){orderId=v;}
    public Long getAnsweredBy(){return answeredBy;} public void setAnsweredBy(Long v){answeredBy=v;}
    public String getCategory(){return category;} public void setCategory(String v){category=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public String getBody(){return body;} public void setBody(String v){body=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getAnswer(){return answer;} public void setAnswer(String v){answer=v;}
    public String getAiState(){return aiState;} public void setAiState(String v){aiState=v;}
    public String getAiToken(){return aiToken;} public void setAiToken(String v){aiToken=v;}
    public String getDraft(){return draft;} public void setDraft(String v){draft=v;}
    public String getReviewReason(){return reviewReason;} public void setReviewReason(String v){reviewReason=v;}
    public String getProductName(){return productName;} public void setProductName(String v){productName=v;}
    public String getOrderNumber(){return orderNumber;} public void setOrderNumber(String v){orderNumber=v;}
    public int getVersion(){return version;} public void setVersion(int v){version=v;}
    public boolean isNeedsReview(){return needsReview;} public void setNeedsReview(boolean v){needsReview=v;}
    public Timestamp getCreatedAt(){return createdAt;} public void setCreatedAt(Timestamp v){createdAt=v;}
    public Timestamp getAnsweredAt(){return answeredAt;} public void setAnsweredAt(Timestamp v){answeredAt=v;}
    public Timestamp getAiStartedAt(){return aiStartedAt;} public void setAiStartedAt(Timestamp v){aiStartedAt=v;}
}
