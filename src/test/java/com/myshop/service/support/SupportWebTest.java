package com.myshop.service.support;

import com.myshop.config.*;
import com.myshop.controller.SupportController;
import com.myshop.domain.*;
import com.myshop.filter.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.jasper.servlet.*;
import org.apache.tomcat.util.descriptor.web.*;
import org.apache.ibatis.mapping.Environment;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SupportWebTest {
    /** Local-only UI preview using synthetic H2 data; never included in the WAR. */
    public static void main(String[] args) throws Exception {
        SupportWebTest preview=new SupportWebTest();preview.start();
        Runtime.getRuntime().addShutdownHook(new Thread(()->{try{preview.stop();}catch(Exception ignored){}}));
        SessionUser customer=SupportServiceTest.user(1,"CUSTOMER"),admin=SupportServiceTest.user(3,"ADMIN");
        preview.service.create(customer,1L,1L,"PRODUCT","머그 세트 사용 방법이 궁금해요","전자레인지에 사용해도 괜찮을까요? 상품 설명에서 확인하지 못해 문의드립니다.");
        long answered=preview.service.create(customer,null,1L,"ORDER","주문 상태를 확인하고 싶어요","결제가 정상적으로 완료되었나요?");
        preview.service.publish(admin,answered,0,"주문은 결제 완료 상태입니다. 배송 진행 상황은 별도로 확인한 뒤 안내해 드리겠습니다.");
        SupportPolicy policy=new SupportPolicy();policy.setTitle("고객 문의 처리 안내");policy.setContent("등록된 자료에 없는 상품 사양과 배송 정보는 담당자가 확인한 후 안내합니다.");policy.setActive(true);preview.service.savePolicy(admin,policy);
        Files.writeString(Path.of("target/support-preview-url.txt"),preview.base,StandardCharsets.UTF_8);
        System.out.println("LOCAL TEST PREVIEW "+preview.base);
        new java.util.concurrent.CountDownLatch(1).await();
    }
    Tomcat tomcat;SupportTestDatabase db;SupportService service;Environment original;String base;
    @BeforeAll void start() throws Exception {
        db=new SupportTestDatabase();
        original=MyBatisProvider.getFactory().getConfiguration().getEnvironment();
        MyBatisProvider.getFactory().getConfiguration().setEnvironment(db.factory.getConfiguration().getEnvironment());
        service=new SupportService(db.factory,new OpenAiDraftClient(HttpClient.newHttpClient(),URI.create("http://127.0.0.1"),"","test",false,java.time.Duration.ofSeconds(1)));
        tomcat=new Tomcat();tomcat.setBaseDir("target/support-web-test");tomcat.setPort(0);tomcat.getConnector().setURIEncoding("UTF-8");tomcat.getConnector().setProperty("address","127.0.0.1");
        Context ctx=tomcat.addContext("/my-shop",Path.of("src/main/webapp").toAbsolutePath().toString());
        ctx.setParentClassLoader(getClass().getClassLoader());
        ctx.addServletContainerInitializer(new JasperInitializer(),null);
        Tomcat.addServlet(ctx,"default",new DefaultServlet()).setLoadOnStartup(1);ctx.addServletMappingDecoded("/","default");
        Tomcat.addServlet(ctx,"jsp",new JspServlet()).setLoadOnStartup(1);ctx.addServletMappingDecoded("*.jsp","jsp");
        Tomcat.addServlet(ctx,"support-status",new com.myshop.controller.SupportStatusController());
        ctx.addServletMappingDecoded("/admin/inquiry/status","support-status");
        Tomcat.addServlet(ctx,"support",new SupportController());
        for(String path:List.of("/account/inquiries","/account/inquiry","/account/inquiry/new","/admin/inquiries","/admin/inquiry","/admin/support/policies")) ctx.addServletMappingDecoded(path,"support");
        Tomcat.addServlet(ctx,"test-session",new HttpServlet(){
            @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException {
                User user=new User();long id=Long.parseLong(req.getParameter("id"));user.setId(id);user.setRole(id==3?"ADMIN":"CUSTOMER");user.setName("테스트");
                req.getSession().setAttribute("loginUser",new SessionUser(user));res.getWriter().write("signed in");
            }
        });ctx.addServletMappingDecoded("/__test/session","test-session");
        filter(ctx,"encoding",new EncodingFilter(),"/*");filter(ctx,"csrf",new CsrfFilter(),"/*");
        filter(ctx,"auth",new AuthFilter(),"/account/*","/admin/*");
        ctx.getServletContext().setAttribute(SupportBootstrap.ATTRIBUTE,service);
        tomcat.start();base="http://127.0.0.1:"+tomcat.getConnector().getLocalPort()+"/my-shop";
    }
    static void filter(Context context,String name,Filter filter,String...paths) {
        FilterDef def=new FilterDef();def.setFilterName(name);def.setFilter(filter);def.setFilterClass(filter.getClass().getName());context.addFilterDef(def);
        FilterMap map=new FilterMap();map.setFilterName(name);for(String p:paths)map.addURLPattern(p);context.addFilterMap(map);
    }
    @AfterAll void stop() throws Exception {
        if(tomcat!=null){tomcat.stop();tomcat.destroy();}if(service!=null)service.close();
        if(original!=null)MyBatisProvider.getFactory().getConfiguration().setEnvironment(original);if(db!=null)db.close();
    }
    HttpClient client(long id) throws Exception {
        HttpClient c=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();
        if(id>0) assertEquals(200,get(c,"/__test/session?id="+id).statusCode());return c;
    }
    HttpResponse<String> get(HttpClient c,String path) throws Exception {
        return c.send(HttpRequest.newBuilder(URI.create(base+path)).GET().build(),HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
    HttpResponse<String> post(HttpClient c,String path,Map<String,String> values) throws Exception {
        StringJoiner form=new StringJoiner("&");values.forEach((k,v)->form.add(URLEncoder.encode(k,StandardCharsets.UTF_8)+"="+URLEncoder.encode(v,StandardCharsets.UTF_8)));
        return c.send(HttpRequest.newBuilder(URI.create(base+path)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(form.toString())).build(),HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
    String csrf(String html) {
        Matcher match=Pattern.compile("name=\"csrfToken\" value=\"([^\"]+)\"").matcher(html);assertTrue(match.find(),html);return match.group(1);
    }
    @Test void realFiltersDenyGuestAdminAndMissingCsrf() throws Exception {
        assertEquals(302,get(client(0),"/account/inquiries").statusCode());
        HttpClient customer=client(1);assertEquals(403,get(customer,"/admin/inquiries").statusCode());
        assertEquals(403,post(customer,"/account/inquiry/new",Map.of("category","OTHER","title","문의","body","내용")).statusCode());
    }
    @Test void customerAndAdminCanCompleteTheRenderedWorkflow() throws Exception {
        HttpClient customer=client(1),admin=client(3),other=client(2);
        var form=get(customer,"/account/inquiry/new?productId=1");assertEquals(200,form.statusCode(),form.body());assertTrue(form.body().contains("무엇이 궁금하세요?"),form.body());
        var created=post(customer,"/account/inquiry/new",Map.of("csrfToken",csrf(form.body()),"category","PRODUCT","productId","1","orderId","1","title","머그 문의","body","<script>alert('x')</script> 재질이 궁금합니다."));
        assertEquals(302,created.statusCode(),created.body());String location=created.headers().firstValue("location").orElseThrow().replace("/my-shop","");
        String inquiryId=location.split("id=")[1].split("&")[0];
        var detail=get(customer,location);assertEquals(200,detail.statusCode(),detail.body());
        assertFalse(detail.body().contains("<script>alert("));assertTrue(detail.body().contains("&lt;script&gt;"));
        assertFalse(detail.body().contains("AI 생성 이력"));assertEquals(404,get(other,location).statusCode());
        var adminPage=get(admin,"/admin/inquiry?id="+inquiryId);assertEquals(200,adminPage.statusCode(),adminPage.body());
        var published=post(admin,"/admin/inquiry",Map.of("csrfToken",csrf(adminPage.body()),"id",inquiryId,"version","0","action","publish","answer","상품 설명을 확인했으며 도자기 머그입니다."));
        assertEquals(302,published.statusCode(),published.body());
        assertTrue(get(customer,location).body().contains("도자기 머그입니다."));
        var list=get(admin,"/admin/inquiries?status=ANSWERED");assertEquals(200,list.statusCode(),list.body());assertTrue(list.body().contains("머그 문의"));
        var policyForm=get(admin,"/admin/support/policies");assertEquals(200,policyForm.statusCode(),policyForm.body());assertTrue(policyForm.body().contains("새 정책 추가"),policyForm.body());
        var policy=post(admin,"/admin/support/policies",Map.of("csrfToken",csrf(policyForm.body()),"version","0","title","반품 문의 안내","content","담당자가 상품 상태를 확인한 후 안내합니다.","active","true"));
        assertEquals(302,policy.statusCode(),policy.body());assertTrue(get(admin,"/admin/support/policies").body().contains("반품 문의 안내"));
        assertEquals(200,get(customer,"/account/inquiries").statusCode());
        assertEquals(200,get(customer,"/assets/css/support.css").statusCode());
    }

    @Test void statusEndpointIsAdminOnlyAndDoesNotCacheDrafts() throws Exception {
        long id=service.create(SupportServiceTest.user(2,"CUSTOMER"),null,null,"OTHER","상태 조회 테스트","내용");
        HttpClient admin=client(3);
        var response=get(admin,"/admin/inquiry/status?id="+id);
        assertEquals(200,response.statusCode(),response.body());
        assertEquals("no-store",response.headers().firstValue("cache-control").orElseThrow());
        var json=com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
        assertTrue(json.get("success").getAsBoolean());assertEquals("DISABLED",json.getAsJsonObject("data").get("state").getAsString());
        assertFalse(json.getAsJsonObject("data").has("aiToken"));
        assertEquals(403,get(client(1),"/admin/inquiry/status?id="+id).statusCode());
        assertEquals(302,get(client(0),"/admin/inquiry/status?id="+id).statusCode());
        assertEquals(404,get(admin,"/admin/inquiry/status?id=99999999").statusCode());
        assertEquals(400,get(admin,"/admin/inquiry/status?id=bad").statusCode());
    }
    @Test void searchFiltersDashboardAndEditorRenderThroughHttp() throws Exception {
        HttpClient admin=client(3),customer=client(2);
        long id=service.create(SupportServiceTest.user(2,"CUSTOMER"),null,null,"OTHER","HTTP 검색 전용제목","필터 본문");
        String keyword=URLEncoder.encode("HTTP 검색 전용제목",StandardCharsets.UTF_8);
        var page=get(admin,"/admin/inquiries?keyword="+keyword+"&category=OTHER&sort=oldest");
        assertEquals(200,page.statusCode(),page.body());assertTrue(page.body().contains("24시간 이상 대기"),page.body());
        assertTrue(page.body().contains("HTTP 검색 전용제목"));assertTrue(page.body().contains("AI 확인 필요"));
        assertEquals(400,get(admin,"/admin/inquiries?page=999999999999999999").statusCode());
        assertEquals(400,get(admin,"/admin/inquiries?sort=notvalid").statusCode());
        assertEquals(403,get(customer,"/account/inquiries?attention=ai").statusCode());
        var editor=get(admin,"/admin/inquiry?id="+id);
        assertEquals(200,editor.statusCode(),editor.body());assertTrue(editor.body().contains("data-reply-editor"));
        assertTrue(editor.body().contains("보관한 답변 복원"));assertTrue(editor.body().contains("support-editor.js"));
        var own=get(customer,"/account/inquiry?id="+id);
        assertFalse(own.body().contains("data-reply-editor"));assertFalse(own.body().contains("24시간 이상 대기"));
    }

    @Test void tamperedOrderAndMalformedIdAreHandled() throws Exception {
        HttpClient customer=client(1);String token=csrf(get(customer,"/account/inquiry/new").body());
        var denied=post(customer,"/account/inquiry/new",Map.of("csrfToken",token,"category","ORDER","title","주문 문의","body","배송 문의","orderId","2"));
        assertEquals(400,denied.statusCode(),denied.body());assertTrue(denied.body().contains("선택한 주문을 확인할 수 없습니다."));
        assertEquals(400,get(customer,"/account/inquiry?id=bad").statusCode());
    }
}
