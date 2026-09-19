package com.myshop.domain;
import java.util.List;
public final class InquiryPage {
    private final List<Inquiry> items;
    private final long totalCount;
    private final int page,totalPages;
    public InquiryPage(List<Inquiry> items,long totalCount,int page,int totalPages){
        this.items=List.copyOf(items);this.totalCount=totalCount;this.page=page;this.totalPages=totalPages;
    }
    public List<Inquiry> getItems(){return items;} public long getTotalCount(){return totalCount;}
    public int getPage(){return page;} public int getTotalPages(){return totalPages;}
    public boolean isHasPrevious(){return page>1;} public boolean isHasNext(){return page<totalPages;}
}
