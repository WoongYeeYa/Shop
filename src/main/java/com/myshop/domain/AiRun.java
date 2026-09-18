package com.myshop.domain;

import java.sql.Timestamp;

public class AiRun {
    private Long id, inquiryId;
    private String state, model, promptVersion, draft, reason, sources, citations;
    private boolean needsReview;
    private long elapsedMs;
    private int inputTokens, outputTokens;
    private Timestamp createdAt;
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Long getInquiryId(){return inquiryId;} public void setInquiryId(Long v){inquiryId=v;}
    public String getState(){return state;} public void setState(String v){state=v;}
    public String getModel(){return model;} public void setModel(String v){model=v;}
    public String getPromptVersion(){return promptVersion;} public void setPromptVersion(String v){promptVersion=v;}
    public String getDraft(){return draft;} public void setDraft(String v){draft=v;}
    public String getReason(){return reason;} public void setReason(String v){reason=v;}
    public String getSources(){return sources;} public void setSources(String v){sources=v;}
    public String getCitations(){return citations;} public void setCitations(String v){citations=v;}
    public boolean isNeedsReview(){return needsReview;} public void setNeedsReview(boolean v){needsReview=v;}
    public long getElapsedMs(){return elapsedMs;} public void setElapsedMs(long v){elapsedMs=v;}
    public int getInputTokens(){return inputTokens;} public void setInputTokens(int v){inputTokens=v;}
    public int getOutputTokens(){return outputTokens;} public void setOutputTokens(int v){outputTokens=v;}
    public Timestamp getCreatedAt(){return createdAt;} public void setCreatedAt(Timestamp v){createdAt=v;}
}
