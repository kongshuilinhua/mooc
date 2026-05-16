package com.elysia.mooc.ai.model;

import java.util.List;

/** OpenAI 兼容聊天模型请求。 */
public record ChatCompletionRequest(String model, List<ChatCompletionMessage> messages) {
}
