package com.elysia.mooc.ai.model;

/** OpenAI 兼容聊天模型结果。 */
public record ChatCompletionResult(
        String content,
        String model,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens,
        String finishReason) {
}
