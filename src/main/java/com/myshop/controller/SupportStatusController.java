package com.myshop.controller;


import com.myshop.config.SupportBootstrap;
import com.myshop.domain.Inquiry;
import com.myshop.service.support.SupportService;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name="supportStatusController",urlPatterns="/admin/inquiry/status")
public final class SupportStatusController extends HttpServlet {
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control","no-store");
        try {
            long id=Long.parseLong(request.getParameter("id"));
            if(id<1) throw new IllegalArgumentException();
            SupportService service=(SupportService)getServletContext().getAttribute(SupportBootstrap.ATTRIBUTE);
            Inquiry q=service.get(ControllerSupport.user(request),id,true);
            if(q==null){send(response,404,false,null,"문의가 없습니다.");return;}
            Map<String,Object> data=new LinkedHashMap<>();
            data.put("state",q.getAiState());data.put("status",q.getStatus());data.put("version",q.getVersion());
            data.put("draft",q.getDraft());data.put("needsReview",q.isNeedsReview());data.put("reason",q.getReviewReason());
            boolean expired=q.getAiStartedAt()!=null && q.getAiStartedAt().getTime()<System.currentTimeMillis()-180000;
            data.put("expired",expired && "RUNNING".equals(q.getAiState()));
            send(response,200,true,data,null);
        } catch(SecurityException ex){send(response,403,false,null,"관리자 권한이 필요합니다.");}
        catch(IllegalArgumentException ex){send(response,400,false,null,"올바른 문의 번호를 입력해 주세요.");}
        catch(RuntimeException ex){getServletContext().log("Support status lookup failed",ex);send(response,500,false,null,"상태를 확인하지 못했습니다.");}
    }
    private static void send(HttpServletResponse response,int status,boolean success,Object data,String message) throws IOException {
        response.setStatus(status);Map<String,Object> body=new LinkedHashMap<>();
        body.put("success",success);body.put("data",data);body.put("message",message);
        response.getWriter().write(new com.google.gson.GsonBuilder().serializeNulls().create().toJson(body));
    }
}
