package com.myshop.domain;

import java.sql.Timestamp;

public class SupportPolicy {
    private Long id;
    private String title, content;
    private boolean active;
    private int version;
    private Timestamp updatedAt;
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public String getContent(){return content;} public void setContent(String v){content=v;}
    public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
    public int getVersion(){return version;} public void setVersion(int v){version=v;}
    public Timestamp getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Timestamp v){updatedAt=v;}
}
