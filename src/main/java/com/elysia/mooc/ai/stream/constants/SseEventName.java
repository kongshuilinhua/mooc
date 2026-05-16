package com.elysia.mooc.ai.stream.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** day17 SSE 事件名称。 */
@Getter
@RequiredArgsConstructor
public enum SseEventName {

    /** 流式响应开始。 */
    START("start"),

    /** RAG 引用来源。 */
    CITATION("citation"),

    /** 回答增量片段。 */
    MESSAGE("message"),

    /** day18 工具调用事件预留。 */
    TOOL_CALL("tool_call"),

    /** 流式响应正常结束。 */
    DONE("done"),

    /** 流式响应异常结束。 */
    ERROR("error");

    /** SSE event 字段值。 */
    private final String value;
}
