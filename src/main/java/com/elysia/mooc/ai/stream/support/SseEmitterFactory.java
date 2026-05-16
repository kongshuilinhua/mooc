package com.elysia.mooc.ai.stream.support;

import com.elysia.mooc.ai.stream.constants.SseEventName;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** SSE 连接创建和事件发送工厂。 */
@Slf4j
@Component
public class SseEmitterFactory {

    private static final long DEFAULT_TIMEOUT_MILLIS = 180_000L;

    private final SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("ai-stream-");

    /**
     * 创建统一超时时间和断开回调的 SSE 连接。
     *
     * @param closeHandler 客户端断开、超时或异常时的收尾逻辑
     * @return SSE 连接对象
     */
    public SseEmitter create(Runnable closeHandler) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MILLIS);
        emitter.onCompletion(closeHandler);
        emitter.onTimeout(closeHandler);
        emitter.onError(error -> closeHandler.run());
        return emitter;
    }

    /**
     * 异步执行流式生成任务。
     *
     * @param task 流式生成任务
     */
    public void execute(Runnable task) {
        executor.execute(task);
    }

    /**
     * 发送 SSE 事件。
     *
     * @param emitter SSE 连接
     * @param eventName 事件名
     * @param data 事件数据
     */
    public void send(SseEmitter emitter, SseEventName eventName, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName.getValue())
                    .data(data, MediaType.APPLICATION_JSON));
        } catch (IOException | IllegalStateException ex) {
            log.warn("发送 SSE 事件失败，event={}", eventName.getValue(), ex);
            throw new IllegalStateException("流式事件发送失败", ex);
        }
    }

    /**
     * 正常完成 SSE 连接。
     *
     * @param emitter SSE 连接
     */
    public void complete(SseEmitter emitter) {
        emitter.complete();
    }
}
