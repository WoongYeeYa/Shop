package com.myshop.domain;
public final class SupportStats {
    private long openCount,overdueCount,aiAttentionCount,answeredTodayCount;
    public long getOpenCount(){return openCount;} public void setOpenCount(long v){openCount=v;}
    public long getOverdueCount(){return overdueCount;} public void setOverdueCount(long v){overdueCount=v;}
    public long getAiAttentionCount(){return aiAttentionCount;} public void setAiAttentionCount(long v){aiAttentionCount=v;}
    public long getAnsweredTodayCount(){return answeredTodayCount;} public void setAnsweredTodayCount(long v){answeredTodayCount=v;}
}
