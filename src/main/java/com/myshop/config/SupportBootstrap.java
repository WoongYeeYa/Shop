package com.myshop.config;

import com.myshop.service.support.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import org.apache.ibatis.session.SqlSession;

@WebListener
public final class SupportBootstrap implements ServletContextListener {
    public static final String ATTRIBUTE=SupportService.class.getName();
    @Override public void contextInitialized(ServletContextEvent event) {
        // Additive and idempotent. Never resets products, users, orders or policies.
        try(InputStream input=SupportBootstrap.class.getResourceAsStream("/db/support.sql");
            SqlSession session=MyBatisProvider.getFactory().openSession(false)) {
            if(input==null) throw new IOException("Missing support migration");
            String sql=new String(input.readAllBytes(),StandardCharsets.UTF_8);
            try(Statement statement=session.getConnection().createStatement()) {
                for(String part:sql.split(";")) if(!part.isBlank()) statement.execute(part);
            }
            session.commit();
        } catch(Exception ex) {throw new IllegalStateException("Support schema initialization failed",ex);}
        event.getServletContext().setAttribute(ATTRIBUTE,new SupportService(MyBatisProvider.getFactory(),new OpenAiDraftClient()));
    }
    @Override public void contextDestroyed(ServletContextEvent event) {
        Object service=event.getServletContext().getAttribute(ATTRIBUTE);
        if(service instanceof SupportService support) support.close();
    }
}
