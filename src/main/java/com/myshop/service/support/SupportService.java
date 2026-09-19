package com.myshop.service.support;

import com.google.gson.Gson;
import com.myshop.domain.*;
import com.myshop.mapper.*;
import org.apache.ibatis.session.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public final class SupportService implements AutoCloseable {
    private final SqlSessionFactory factory;
    private final AiDraftClient client;
    private final ExecutorService workers;
    private static final Set<String> CATEGORIES=Set.of("PRODUCT","ORDER","RETURN","OTHER");
    public SupportService(SqlSessionFactory factory,AiDraftClient client) {
        this(factory,client,new ThreadPoolExecutor(2,2,0,TimeUnit.SECONDS,new ArrayBlockingQueue<>(20),
            task->{Thread t=new Thread(task,"support-ai");t.setDaemon(true);return t;},new ThreadPoolExecutor.AbortPolicy()));
    }
    public SupportService(SqlSessionFactory factory,AiDraftClient client,ExecutorService workers) {
        this.factory=factory;this.client=client;this.workers=workers;
    }
    public boolean aiEnabled(){return client.enabled();}
    public static String required(String value,int limit,String label) {
        if(value==null || value.isBlank() || value.trim().length()>limit) throw new IllegalArgumentException(label+"을(를) 1~"+limit+"자로 입력해 주세요.");
        return value.trim();
    }
    private static void requireUser(SessionUser actor) {if(actor==null) throw new SecurityException("로그인이 필요합니다.");}
    private static void requireAdmin(SessionUser actor) {requireUser(actor);if(!actor.isAdmin()) throw new SecurityException("관리자만 사용할 수 있습니다.");}
    public long create(SessionUser actor,Long productId,Long orderId,String category,String title,String body) {
        requireUser(actor);
        if(!CATEGORIES.contains(category)) throw new IllegalArgumentException("문의 유형을 선택해 주세요.");
        Inquiry q=new Inquiry();q.setUserId(actor.getId());q.setProductId(productId);q.setOrderId(orderId);
        q.setCategory(category);q.setTitle(required(title,120,"제목"));q.setBody(required(body,4000,"문의 내용"));
        q.setAiState(aiEnabled()?"PENDING":"DISABLED");
        try(SqlSession session=factory.openSession(false)) {
            SupportMapper m=session.getMapper(SupportMapper.class);
            if(m.lockUser(actor.getId())==null) throw new SecurityException("계정을 확인해 주세요.");
            if(m.countRecent(actor.getId(),Timestamp.from(Instant.now().minusSeconds(600)))>=5)
                throw new IllegalArgumentException("10분 동안 최대 5건까지 접수할 수 있습니다. 잠시 후 다시 시도해 주세요.");
            if(orderId!=null && m.ownedOrder(orderId,actor.getId())==null) throw new IllegalArgumentException("선택한 주문을 확인할 수 없습니다.");
            if(productId!=null) {
                Product p=session.getMapper(ProductMapper.class).findByIdAdmin(productId);
                if(p==null || (!p.isActive() && (orderId==null || m.orderHasProduct(orderId,productId)==0)))
                    throw new IllegalArgumentException("문의할 수 없는 상품입니다.");
                if(orderId!=null && m.orderHasProduct(orderId,productId)==0) throw new IllegalArgumentException("해당 주문에 포함되지 않은 상품입니다.");
            }
            m.insertInquiry(q);session.commit();
        }
        // Commit first. A scheduling failure must not cause the user to submit the inquiry again.
        if(aiEnabled()) {
            try {schedule(q.getId());} catch(RuntimeException ignored) {
                System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING,"AI scheduling unavailable; inquiry retained for manual processing.");
            }
        }
        return q.getId();
    }
    public Inquiry get(SessionUser actor,long id,boolean admin) {
        requireUser(actor);if(admin) requireAdmin(actor);
        try(SqlSession session=factory.openSession()) {
            Inquiry q=session.getMapper(SupportMapper.class).find(id,admin?null:actor.getId());
            if(q!=null && !admin) redact(q);
            return q;
        }
    }
    public List<Inquiry> list(SessionUser actor,boolean admin,String status,int page) {
        return search(actor,admin,new InquirySearch(status,null,null,null,null,page)).getItems();
    }
    public InquiryPage search(SessionUser actor,boolean admin,InquirySearch filter) {
        requireUser(actor);if(admin) requireAdmin(actor);
        if(!admin && filter.getAttention()!=null) throw new SecurityException("관리자 전용 필터입니다.");
        try(SqlSession session=factory.openSession()) {
            SupportMapper mapper=session.getMapper(SupportMapper.class);
            Long owner=admin?null:actor.getId();
            long total=mapper.countSearch(owner,filter);
            int totalPages=(int)Math.min(Integer.MAX_VALUE,Math.max(1,(total+19)/20));
            int page=Math.min(filter.getPage(),totalPages);
            List<Inquiry> result=mapper.search(owner,filter,(page-1)*20L);
            if(!admin) result.forEach(SupportService::redact);
            return new InquiryPage(result,total,page,totalPages);
        }
    }
    public SupportStats stats(SessionUser actor) {
        requireAdmin(actor);
        try(SqlSession session=factory.openSession()) {
            return session.getMapper(SupportMapper.class).stats(new InquirySearch(null,null,null,null,null,1));
        }
    }
    private static void redact(Inquiry q) {
        q.setDraft(null);q.setReviewReason(null);q.setAiToken(null);q.setAiState(null);q.setAnsweredBy(null);
    }
    public void publish(SessionUser actor,long id,int version,String answer) {
        requireAdmin(actor);answer=required(answer,5000,"답변");
        try(SqlSession session=factory.openSession(false)) {
            if(session.getMapper(SupportMapper.class).publish(id,actor.getId(),answer,version)!=1)
                throw new IllegalStateException("다른 관리자가 답변을 변경했습니다. 새로고침 후 다시 확인해 주세요.");
            session.commit();
        }
    }
    public List<SupportPolicy> policies(SessionUser actor) {
        requireAdmin(actor);
        try(SqlSession session=factory.openSession()) {return session.getMapper(SupportMapper.class).policies(false);}
    }
    public void savePolicy(SessionUser actor,SupportPolicy policy) {
        requireAdmin(actor);
        policy.setTitle(required(policy.getTitle(),120,"정책 제목"));policy.setContent(required(policy.getContent(),6000,"정책 내용"));
        try(SqlSession session=factory.openSession(false)) {
            SupportMapper m=session.getMapper(SupportMapper.class);
            if(policy.getId()==null) {
                // The policy table remains deliberately small for bounded prompt context.
                if(m.policies(false).size()>=12) throw new IllegalArgumentException("정책은 최대 12개입니다. 기존 정책을 수정해 주세요.");
                m.insertPolicy(policy);
            } else if(m.updatePolicy(policy)!=1) throw new IllegalStateException("정책이 변경되었습니다. 새로고침 후 다시 저장해 주세요.");
            session.commit();
        }
    }
    public List<AiRun> runs(SessionUser actor,long id) {
        requireAdmin(actor);
        try(SqlSession session=factory.openSession()) {return session.getMapper(SupportMapper.class).runs(id);}
    }
    public void regenerate(SessionUser actor,long id) {
        requireAdmin(actor);if(!aiEnabled()) throw new IllegalStateException("AI가 연결되지 않았습니다. 수동으로 답변할 수 있습니다.");schedule(id);
    }
    private void schedule(long id) {
        String token=UUID.randomUUID().toString();
        try(SqlSession session=factory.openSession(false)) {
            SupportMapper m=session.getMapper(SupportMapper.class);
            if(m.runCount(id)>=20) throw new IllegalStateException("이 문의의 AI 생성 한도(20회)에 도달했습니다.");
            if(m.claim(id,token,Timestamp.from(Instant.now().minusSeconds(30)),Timestamp.from(Instant.now().minusSeconds(180)))!=1)
                throw new IllegalStateException("이미 처리 중이거나 답변 완료된 문의입니다. 재생성은 30초 후 가능합니다. 중단된 작업은 3분 후 재시도하세요.");
            session.commit();
        }
        try {workers.execute(()->generate(id,token));}
        catch(RejectedExecutionException ex) {
            try(SqlSession session=factory.openSession(true)) {session.getMapper(SupportMapper.class).failClaim(id,token);}
            throw new IllegalStateException("AI 작업이 많습니다. 잠시 후 다시 시도해 주세요.");
        }
    }
    private void generate(long id,String token) {
        long start=System.nanoTime();List<AiDraftClient.Source> sources=new ArrayList<>();AiRun run;
        try {
            Inquiry q;
            try(SqlSession session=factory.openSession()) {
                SupportMapper m=session.getMapper(SupportMapper.class);q=m.find(id,null);
                if(q==null || !token.equals(q.getAiToken()) || !"OPEN".equals(q.getStatus())) return;
                if(q.getProductId()!=null) {
                    Product p=session.getMapper(ProductMapper.class).findByIdAdmin(q.getProductId());
                    if(p!=null) sources.add(new AiDraftClient.Source("product:"+p.getId(),p.getName(),
                        "상품 설명: "+p.getDescription()+"\n현재 가격: "+p.getPrice()+"원\n현재 재고: "+p.getStock()+"\n판매 중: "+p.isActive()));
                }
                if(q.getOrderId()!=null) {
                    OrderSummary o=m.ownedOrder(q.getOrderId(),q.getUserId());
                    if(o!=null) sources.add(new AiDraftClient.Source("order:"+o.getId(),"본인 주문 상태",
                        "결제 상태: "+o.getStatus()+"\n주문일: "+o.getCreatedAt()+"\n주문 상품: "+m.orderItems(o.getId())
                        +"\n출고일, 배송 추적, 수령일은 저장되어 있지 않음. PAID는 결제 완료만 의미함."));
                }
                // Hard cap also protects deployments with policies inserted outside the application.
                for(SupportPolicy p:m.policies(true).stream().limit(12).toList())
                    sources.add(new AiDraftClient.Source("policy:"+p.getId()+":v"+p.getVersion(),p.getTitle(),p.getContent()));
            }
            run=client.generate(q,sources);
        } catch(Exception ex) {
            if(ex instanceof InterruptedException) Thread.currentThread().interrupt();
            run=new AiRun();run.setState("FAILED");run.setNeedsReview(true);
            run.setReason("AI 초안을 생성하지 못했습니다. 연결 설정을 확인하거나 수동으로 답변해 주세요.");
            run.setSources(new Gson().toJson(sources));run.setCitations("[]");
        }
        run.setInquiryId(id);run.setModel(client.model());run.setPromptVersion(OpenAiDraftClient.PROMPT_VERSION);
        run.setElapsedMs(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start));
        try(SqlSession session=factory.openSession(false)) {
            SupportMapper m=session.getMapper(SupportMapper.class);
            // Late results never overwrite an approved answer or a newer generation.
            if(m.finish(id,token,run)==0) run.setState("SUPERSEDED");
            m.insertRun(run);session.commit();
        } catch(RuntimeException ex) {
            System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR,"AI result persistence failed; lease permits retry.");
        }
    }
    public void close(){workers.shutdownNow();}
}
