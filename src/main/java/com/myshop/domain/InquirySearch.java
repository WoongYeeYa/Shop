package com.myshop.domain;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Set;

/** Validated search input. No raw SQL identifiers come from request parameters. */
public final class InquirySearch {
    private final String status, category, keyword, sort, attention;
    private final int page;
    private final Timestamp today=Timestamp.from(java.time.LocalDate.now(java.time.ZoneId.of("Asia/Seoul")).atStartOfDay(java.time.ZoneId.of("Asia/Seoul")).toInstant());
    private final Timestamp overdueBefore=Timestamp.from(Instant.now().minusSeconds(86400));
    private final Timestamp expiredBefore=Timestamp.from(Instant.now().minusSeconds(180));
    public InquirySearch(String status,String category,String keyword,String sort,String attention,int page) {
        this.status=clean(status);this.category=clean(category);this.keyword=clean(keyword);
        this.sort=clean(sort)==null?"newest":sort.trim();this.attention=clean(attention);this.page=page;
        if(this.status!=null && !Set.of("OPEN","ANSWERED").contains(this.status)) throw new IllegalArgumentException("잘못된 상태입니다.");
        if(this.category!=null && !Set.of("PRODUCT","ORDER","RETURN","OTHER").contains(this.category)) throw new IllegalArgumentException("잘못된 문의 유형입니다.");
        if(this.keyword!=null && this.keyword.length()>100) throw new IllegalArgumentException("검색어는 100자까지 입력해 주세요.");
        if(!Set.of("newest","oldest").contains(this.sort)) throw new IllegalArgumentException("잘못된 정렬입니다.");
        if(this.attention!=null && !Set.of("overdue","ai","today").contains(this.attention)) throw new IllegalArgumentException("잘못된 확인 항목입니다.");
        if(page<1 || page>1000000) throw new IllegalArgumentException("잘못된 페이지입니다.");
    }
    private static String clean(String value){return value==null || value.isBlank()?null:value.trim();}
    public String getStatus(){return status;} public String getCategory(){return category;}
    public String getKeyword(){return keyword;} public String getSort(){return sort;}
    public String getAttention(){return attention;} public int getPage(){return page;}
    public String getPattern(){return keyword==null?null:keyword.replace("=","==").replace("%","=%").replace("_","=_");}
    public Timestamp getOverdueBefore(){return overdueBefore;} public Timestamp getExpiredBefore(){return expiredBefore;}
    public Timestamp getToday(){return today;}
}
