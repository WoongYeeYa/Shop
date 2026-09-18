package com.myshop.service.support;
import com.myshop.domain.*;
import java.util.List;
public interface AiDraftClient {
    record Source(String id, String title, String content) {}
    boolean enabled();
    String model();
    AiRun generate(Inquiry inquiry, List<Source> sources) throws Exception;
}
