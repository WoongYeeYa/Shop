package com.myshop.controller;

import com.myshop.config.SupportBootstrap;
import com.myshop.domain.*;
import com.myshop.service.impl.*;
import com.myshop.service.support.SupportService;
import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name="supportController",urlPatterns={"/account/inquiries","/account/inquiry","/account/inquiry/new","/admin/inquiries","/admin/inquiry","/admin/support/policies"})
public final class SupportController extends HttpServlet {
    private SupportService service(){return (SupportService)getServletContext().getAttribute(SupportBootstrap.ATTRIBUTE);}
    private boolean admin(HttpServletRequest r){return r.getServletPath().startsWith("/admin/");}
    private static long id(String value) {
        try {long parsed=Long.parseLong(value);if(parsed>0) return parsed;} catch(RuntimeException ignored) {}
        throw new IllegalArgumentException("올바른 번호를 입력해 주세요.");
    }
    private static Long optionalId(String value){return value==null || value.isBlank()?null:id(value);}
    private static int version(String value) {
        try {int parsed=Integer.parseInt(value);if(parsed>=0) return parsed;} catch(RuntimeException ignored) {}
        throw new IllegalArgumentException("화면을 새로고침해 주세요.");
    }
    private void view(HttpServletRequest r,HttpServletResponse s,String name) throws ServletException,IOException {
        r.setAttribute("adminView",admin(r));r.setAttribute("aiEnabled",service().aiEnabled());
        r.getRequestDispatcher("/WEB-INF/views/support/"+name+".jsp").forward(r,s);
    }
    @Override protected void doGet(HttpServletRequest r,HttpServletResponse s) throws ServletException,IOException {
        try {render(r,s);}
        catch(IllegalArgumentException ex){s.sendError(400);}
        catch(SecurityException ex){s.sendError(403);}
        catch(RuntimeException ex){getServletContext().log("Support page failed",ex);s.sendError(500);}
    }
    private void render(HttpServletRequest r,HttpServletResponse s) throws ServletException,IOException {
        SessionUser user=ControllerSupport.user(r);String path=r.getServletPath();
        if(path.endsWith("/new")) {
            // Options are scoped to the signed-in user. Submission validates them again.
            r.setAttribute("products",new ProductServiceImpl().getAllAdmin().stream().filter(Product::isActive).toList());
            r.setAttribute("orders",new OrderServiceImpl().getOrders(user.getId()));
            r.setAttribute("pageTitle","문의 작성");view(r,s,"new");
        } else if(path.endsWith("/policies")) {
            r.setAttribute("policies",service().policies(user));r.setAttribute("pageTitle","상담 정책 관리");view(r,s,"policies");
        } else if(path.endsWith("/inquiries")) {
            String raw=r.getParameter("page");int page=raw==null?1:Math.toIntExact(id(raw));
            String status=ControllerSupport.value(r,"status");if(status.isBlank()) status=null;
            r.setAttribute("inquiries",service().list(user,admin(r),status,page));r.setAttribute("currentPage",page);
            r.setAttribute("statusFilter",status);r.setAttribute("pageTitle",admin(r)?"문의 관리":"내 문의");view(r,s,"list");
        } else {
            Inquiry q=service().get(user,id(r.getParameter("id")),admin(r));
            if(q==null){s.sendError(404);return;}
            r.setAttribute("inquiry",q);
            if(admin(r)) {
                r.setAttribute("runs",service().runs(user,q.getId()));
                String value=r.getParameter("answer");
                r.setAttribute("answerText",value!=null?value:q.getAnswer()!=null?q.getAnswer():q.getDraft());
            }
            r.setAttribute("pageTitle",admin(r)?"문의 검토":"문의 상세");view(r,s,"detail");
        }
    }
    @Override protected void doPost(HttpServletRequest r,HttpServletResponse s) throws ServletException,IOException {
        try {
            SessionUser user=ControllerSupport.user(r);String path=r.getServletPath();
            if("/account/inquiry/new".equals(path)) {
                long inquiryId=service().create(user,optionalId(r.getParameter("productId")),optionalId(r.getParameter("orderId")),
                    ControllerSupport.value(r,"category"),ControllerSupport.value(r,"title"),ControllerSupport.value(r,"body"));
                s.sendRedirect(r.getContextPath()+"/account/inquiry?id="+inquiryId+"&saved=1");
            } else if("/admin/inquiry".equals(path)) {
                long inquiryId=id(r.getParameter("id"));String action=ControllerSupport.value(r,"action");
                if("generate".equals(action)) service().regenerate(user,inquiryId);
                else if("publish".equals(action)) service().publish(user,inquiryId,version(r.getParameter("version")),r.getParameter("answer"));
                else throw new IllegalArgumentException("지원하지 않는 작업입니다.");
                s.sendRedirect(r.getContextPath()+"/admin/inquiry?id="+inquiryId+"&saved=1");
            } else if("/admin/support/policies".equals(path)) {
                SupportPolicy policy=new SupportPolicy();policy.setId(optionalId(r.getParameter("id")));
                policy.setVersion(version(r.getParameter("version")));policy.setTitle(r.getParameter("title"));policy.setContent(r.getParameter("content"));
                policy.setActive("true".equals(r.getParameter("active")));
                r.setAttribute("failedPolicy",policy);service().savePolicy(user,policy);
                s.sendRedirect(r.getContextPath()+"/admin/support/policies?saved=1");
            } else s.sendError(405);
        } catch(SecurityException ex){s.sendError(403);}
        catch(IllegalArgumentException | IllegalStateException ex) {
            s.setStatus(ex instanceof IllegalStateException?409:400);r.setAttribute("error",ex.getMessage());
            r.setAttribute("conflict",ex instanceof IllegalStateException);
            try {render(r,s);} catch(IllegalArgumentException e){s.sendError(400);}
        } catch(RuntimeException ex){getServletContext().log("Support operation failed",ex);s.sendError(500);}
    }
}
