package com.elysia.mooc.ai.model;

/** AI 普通聊天客户端。 */
public interface AiChatClient {

    /**
     * 调用聊天模型生成回复。
     *
     * @param request OpenAI 兼容聊天请求
     * @return 模型回复结果
     */
    ChatCompletionResult complete(ChatCompletionRequest request);
}
