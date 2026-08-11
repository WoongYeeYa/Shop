package com.myshop.controller;
import com.myshop.domain.SessionUser;import com.myshop.service.CartService;import com.myshop.service.OrderService;import com.myshop.service.impl.CartServiceImpl;import com.myshop.service.impl.OrderServiceImpl;import java.io.IOException;import javax.servlet.*;import javax.servlet.annotation.WebServlet;import javax.servlet.http.*;
@WebServlet(name="checkoutController",urlPatterns={"/account/checkout","/account/orders"})
public class CheckoutController extends HttpServlet{
 private final OrderService service=new OrderServiceImpl();
 protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{if(req.getServletPath().endsWith("orders")){req.setAttribute("orders",service.getOrders(user(req).getId()));req.getRequestDispatcher("/WEB-INF/views/orders.jsp").forward(req,res);}else res.sendRedirect(req.getContextPath()+"/account/cart");}
 protected void doPost(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{try{String number=service.checkout(user(req).getId());req.setAttribute("orderNumber",number);req.getRequestDispatcher("/WEB-INF/views/order-complete.jsp").forward(req,res);}catch(IllegalStateException e){res.setStatus(409);CartService cart=new CartServiceImpl();java.util.List<com.myshop.domain.CartLine> lines=cart.get(user(req).getId());req.setAttribute("checkoutError",e.getMessage());req.setAttribute("lines",lines);req.setAttribute("total",cart.total(lines));req.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(req,res);}catch(RuntimeException e){getServletContext().log("Checkout failed",e);res.sendError(500);}}
 private SessionUser user(HttpServletRequest r){return(SessionUser)r.getSession().getAttribute("loginUser");}
}
