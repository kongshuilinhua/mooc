package com.elysia.mooc.ai.stream.support;

/** 流式消息内存累加器。 */
public class StreamMessageAccumulator {

    private final StringBuilder builder = new StringBuilder();

    /**
     * 追加模型输出片段。
     *
     * @param chunk 本次增量文本，空值会被忽略
     */
    public void append(String chunk) {
        if (chunk != null && !chunk.isEmpty()) {
            builder.append(chunk);
        }
    }

    /**
     * 获取当前完整文本。
     *
     * @return 已累积的完整回答
     */
    public String content() {
        return builder.toString();
    }
}
