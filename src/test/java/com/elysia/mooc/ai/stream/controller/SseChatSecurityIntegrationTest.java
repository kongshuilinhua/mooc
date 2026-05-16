package com.elysia.mooc.ai.stream.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.DispatcherType;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** SSE 收尾阶段的真实安全过滤链回归测试。 */
@SpringBootTest(properties = {
        "mooc.event.message-consumer-auto-startup=false",
        "mooc.qdrant.auto-initialize=false"
})
@AutoConfigureMockMvc
@Import(SseChatSecurityIntegrationTest.DoneSseController.class)
class SseChatSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 容器在 SSE 完成后可能进入 ERROR 分派，安全链不能把内部收尾请求改写成 401/403。
     */
    @Test
    void errorDispatcherShouldNotBeRejectedBySecurity() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/ai/chat/stream")
                        .with(request -> {
                            request.setDispatcherType(DispatcherType.ERROR);
                            return request;
                        }))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isNotIn(401, 403);
    }

    /**
     * 客户端读取到 done 事件后，异步分派应正常完成，不能再追加权限错误导致传输层异常。
     */
    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void doneEventShouldCompleteWithoutSecurityError() throws Exception {
        MvcResult started = mockMvc.perform(post("/api/test/sse/done"))
                .andExpect(request().asyncStarted())
                .andReturn();

        MvcResult completed = mockMvc.perform(asyncDispatch(started))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andReturn();

        String body = completed.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(body)
                .contains("event:done")
                .contains("SUCCESS")
                .doesNotContain("没有权限")
                .doesNotContain("Access Denied");
    }

    /** 测试专用的最小 SSE 端点，只验证 MVC/Security 收尾链路，不进入真实 AI 业务。 */
    @RestController
    static class DoneSseController {

        @PostMapping(value = "/api/test/sse/done", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        SseEmitter done() {
            SseEmitter emitter = new SseEmitter(1000L);
            CompletableFuture.runAsync(() -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("done")
                            .data(Map.of("status", "SUCCESS"), MediaType.APPLICATION_JSON));
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            });
            return emitter;
        }
    }
}
