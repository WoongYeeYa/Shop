package com.myshop.service.support;

import com.google.gson.*;
import com.myshop.domain.*;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OpenAiDraftClientTest {
    private final List<AiDraftClient.Source> sources=List.of(new AiDraftClient.Source("product:1","머그","도자기"));
    static String response(String answer,String citation,boolean review) {
        Gson gson=new Gson();
        String payload=gson.toJson(Map.of("answer",answer,"sourceIds",citation.isEmpty()?List.of():List.of(citation),"needsReview",review,"reason","확인 필요"));
        return gson.toJson(Map.of("status","completed","output",List.of(Map.of("type","message","content",List.of(Map.of("type","output_text","text",payload)))),"usage",Map.of("input_tokens",100,"output_tokens",30)));
    }
    @Test void acceptsGroundedOutputAndRecordsUsage() {
        AiRun r=OpenAiDraftClient.parse(response("도자기입니다.","product:1",false),sources);
        assertEquals("도자기입니다.",r.getDraft());assertEquals(100,r.getInputTokens());assertFalse(r.isNeedsReview());
    }
    @Test void rejectsInventedSourceAndIncompleteOutput() {
        assertThrows(IllegalArgumentException.class,()->OpenAiDraftClient.parse(response("무료 환불","policy:999",false),sources));
        assertThrows(IllegalArgumentException.class,()->OpenAiDraftClient.parse("{\"status\":\"incomplete\"}",sources));
        assertThrows(RuntimeException.class,()->OpenAiDraftClient.parse("broken json",sources));
        assertThrows(IllegalArgumentException.class,()->OpenAiDraftClient.parse(response("","product:1",false),sources));
    }
    @Test void missingEvidenceForcesReview() {
        AiRun r=OpenAiDraftClient.parse(response("담당자 확인이 필요합니다.","",false),sources);
        assertTrue(r.isNeedsReview());
    }
    @Test void refusalIsNotTreatedAsAnAnswer() {
        assertThrows(IllegalArgumentException.class,()->OpenAiDraftClient.parse("""
            {"status":"completed","output":[{"content":[{"type":"refusal","refusal":"cannot answer"}]}]}
            """,sources));
    }
    @Test void actualHttpRequestUsesStrictSchemaAndDoesNotStoreResponse() throws Exception {
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        AtomicReference<JsonObject> sent=new AtomicReference<>();
        server.createContext("/responses",exchange->{
            sent.set(JsonParser.parseString(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8)).getAsJsonObject());
            byte[] body=response("도자기입니다.","product:1",false).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200,body.length);exchange.getResponseBody().write(body);exchange.close();
        });server.start();
        try {
            var client=new OpenAiDraftClient(HttpClient.newHttpClient(),URI.create("http://127.0.0.1:"+server.getAddress().getPort()+"/responses"),"test-key","test-model",true,Duration.ofSeconds(3));
            Inquiry q=new Inquiry();q.setTitle("문의");q.setBody("재질은?");q.setCategory("PRODUCT");
            assertEquals("도자기입니다.",client.generate(q,sources).getDraft());
            assertFalse(sent.get().get("store").getAsBoolean());
            assertTrue(sent.get().getAsJsonObject("text").getAsJsonObject("format").get("strict").getAsBoolean());
            assertFalse(sent.get().has("tools"));
        } finally {server.stop(0);}
    }
    @Test void nonSuccessHttpStatusIsRejectedWithoutLeakingProviderBody() throws Exception {
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/",exchange->{byte[] body="private provider error".getBytes(StandardCharsets.UTF_8);exchange.sendResponseHeaders(429,body.length);exchange.getResponseBody().write(body);exchange.close();});
        server.start();
        try {
            var client=new OpenAiDraftClient(HttpClient.newHttpClient(),URI.create("http://127.0.0.1:"+server.getAddress().getPort()),"test","test-model",true,Duration.ofSeconds(2));
            assertEquals("AI_HTTP_429",assertThrows(IllegalStateException.class,()->client.generate(new Inquiry(),sources)).getMessage());
        } finally {server.stop(0);}
    }
}
