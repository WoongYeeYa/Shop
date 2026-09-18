package com.myshop.service.support;

import com.google.gson.Gson;
import com.myshop.domain.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class SupportServiceTest {
    SupportTestDatabase db;
    SupportService service;
    QueueExecutor executor;
    FakeClient client;
    final SessionUser customer=user(1,"CUSTOMER"),other=user(2,"CUSTOMER"),admin=user(3,"ADMIN");
    static SessionUser user(long id,String role){User u=new User();u.setId(id);u.setRole(role);return new SessionUser(u);}
    @BeforeEach void setup() throws Exception {
        db=new SupportTestDatabase();executor=new QueueExecutor();client=new FakeClient();service=new SupportService(db.factory,client,executor);
    }
    @AfterEach void cleanup(){service.close();db.close();}
    long create(){return service.create(customer,1L,1L,"PRODUCT","상품 문의","머그 재질은 무엇인가요?");}
    @Test void onlyOwnerCanReadAndOtherOrderCannotBeAttached() {
        long id=create();
        assertNotNull(service.get(customer,id,false));assertNull(service.get(other,id,false));
        assertThrows(IllegalArgumentException.class,()->service.create(customer,null,2L,"ORDER","질문","배송 문의"));
        assertThrows(IllegalArgumentException.class,()->service.create(customer,2L,1L,"PRODUCT","질문","상품 문의"));
        assertThrows(SecurityException.class,()->service.get(customer,id,true));
        assertThrows(SecurityException.class,()->service.publish(customer,id,0,"답변"));
        assertThrows(SecurityException.class,()->service.policies(customer));
        assertThrows(SecurityException.class,()->service.runs(customer,id));
        assertThrows(SecurityException.class,()->service.regenerate(customer,id));
    }
    @Test void draftsStayPrivateUntilApprovedAndOriginalIsRetained() {
        client.enabled=true;long id=create();executor.drain();
        assertEquals("READY",service.get(admin,id,true).getAiState());
        assertNull(service.get(customer,id,false).getDraft());
        assertNull(service.get(customer,id,false).getReviewReason());
        assertNull(service.list(customer,false,null,1).get(0).getDraft());
        assertNull(service.get(customer,id,false).getAnswer());
        assertEquals(1,service.runs(admin,id).size());
        service.publish(admin,id,0,"확인한 최종 답변");
        assertEquals("확인한 최종 답변",service.get(customer,id,false).getAnswer());
        assertEquals("도자기 머그 두 개입니다.",service.runs(admin,id).get(0).getDraft());
        assertThrows(IllegalStateException.class,()->service.publish(admin,id,0,"덮어쓰기"));
    }
    @Test void providerFailureDoesNotLoseInquiryOrBlockManualAnswer() {
        client.enabled=true;client.fail=true;long id=create();executor.drain();
        assertEquals("FAILED",service.get(admin,id,true).getAiState());
        assertEquals("FAILED",service.runs(admin,id).get(0).getState());
        service.publish(admin,id,0,"직접 확인한 답변");
        assertEquals("ANSWERED",service.get(customer,id,false).getStatus());
    }
    @Test void disabledAiDoesNotGenerateAFakeDraft() {
        long id=create();assertEquals("DISABLED",service.get(admin,id,true).getAiState());
        assertEquals(0,executor.queue.size());assertTrue(service.runs(admin,id).isEmpty());
        assertThrows(IllegalStateException.class,()->service.regenerate(admin,id));
    }
    @Test void lateAiResultDoesNotOverwriteApprovedAnswer() {
        client.enabled=true;long id=create();
        client.during=()->service.publish(admin,id,0,"담당자 확정 답변");
        executor.drain();
        assertEquals("담당자 확정 답변",service.get(customer,id,false).getAnswer());
        assertEquals("SUPERSEDED",service.runs(admin,id).get(0).getState());
        assertNull(service.get(admin,id,true).getDraft());
    }
    @Test void rejectedQueueKeepsInquiryAndAllowsManualProcessing() {
        client.enabled=true;executor.reject=true;long id=create();
        assertEquals("FAILED",service.get(admin,id,true).getAiState());
        service.publish(admin,id,0,"답변");assertEquals("답변",service.get(customer,id,false).getAnswer());
    }
    @Test void policySnapshotSurvivesEditsAndInactivePoliciesAreExcluded() {
        SupportPolicy policy=new SupportPolicy();policy.setTitle("교환 안내");policy.setContent("담당자가 조건을 확인합니다.");policy.setActive(true);
        service.savePolicy(admin,policy);
        SupportPolicy inactive=new SupportPolicy();inactive.setTitle("미검토");inactive.setContent("보내면 안 되는 정책");service.savePolicy(admin,inactive);
        client.enabled=true;long id=create();executor.drain();
        String original=service.runs(admin,id).get(0).getSources();
        assertTrue(original.contains("담당자가 조건을 확인합니다."));assertFalse(original.contains("보내면 안 되는 정책"));
        policy.setContent("새 정책");service.savePolicy(admin,policy);
        assertEquals(original,service.runs(admin,id).get(0).getSources());
        assertThrows(IllegalStateException.class,()->service.savePolicy(admin,policy));
    }
    @Test void validationAndPerUserRateLimit() {
        assertThrows(IllegalArgumentException.class,()->service.create(customer,null,null,"BAD","제목","내용"));
        assertThrows(IllegalArgumentException.class,()->service.create(customer,null,null,"OTHER"," ","내용"));
        assertThrows(IllegalArgumentException.class,()->service.create(customer,null,null,"OTHER","제목","가".repeat(4001)));
        for(int i=0;i<5;i++) create();
        assertThrows(IllegalArgumentException.class,this::create);
        assertDoesNotThrow(()->service.create(other,null,null,"OTHER","제목","내용"));
    }
    @Test void duplicateGenerationIsRejectedAndExpiredLeaseCanBeRetried() throws Exception {
        client.enabled=true;long id=create();
        assertThrows(IllegalStateException.class,()->service.regenerate(admin,id));
        db.execute("UPDATE inquiries SET ai_started_at=TIMESTAMP '2020-01-01 00:00:00' WHERE id="+id);
        service.regenerate(admin,id);executor.drain();
        assertEquals(1,service.runs(admin,id).size());assertEquals("READY",service.get(admin,id,true).getAiState());
    }
    @Test void concurrentApprovalsHaveOneWinner() throws Exception {
        long id=create();ExecutorService concurrent=Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> publish=()->{try{service.publish(admin,id,0,"답변");return true;}catch(IllegalStateException ex){return false;}};
            List<Future<Boolean>> results=concurrent.invokeAll(List.of(publish,publish));
            assertNotEquals(results.get(0).get(),results.get(1).get());
        } finally {concurrent.shutdownNow();}
    }
    static class FakeClient implements AiDraftClient {
        boolean enabled,fail;Runnable during=()->{};
        public boolean enabled(){return enabled;}public String model(){return "test-model";}
        public AiRun generate(Inquiry q,List<Source> sources) {
            during.run();if(fail) throw new IllegalStateException("simulated outage");
            AiRun r=new AiRun();r.setState("READY");r.setDraft("도자기 머그 두 개입니다.");r.setReason("");r.setSources(new Gson().toJson(sources));r.setCitations("[\"product:1\"]");return r;
        }
    }
    static class QueueExecutor extends AbstractExecutorService {
        final Queue<Runnable> queue=new ArrayDeque<>();boolean reject,closed;
        public void execute(Runnable r){if(reject) throw new RejectedExecutionException();queue.add(r);}
        void drain(){while(!queue.isEmpty()) queue.remove().run();}
        public void shutdown(){closed=true;}public List<Runnable> shutdownNow(){closed=true;queue.clear();return List.of();}
        public boolean isShutdown(){return closed;}public boolean isTerminated(){return closed;}public boolean awaitTermination(long t,TimeUnit u){return closed;}
    }
}
