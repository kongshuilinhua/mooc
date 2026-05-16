package com.elysia.mooc.ai.chat.constants;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** AI 普通聊天模块错误码。 */
@Getter
@RequiredArgsConstructor
public enum AiChatErrorCode implements ErrorCode {

    /** 请求参数不正确。 */
    AI_CHAT_PARAM_INVALID(15001, "聊天参数不正确"),

    /** 会话不存在。 */
    AI_CONVERSATION_NOT_FOUND(15002, "AI 会话不存在"),

    /** 无权访问会话。 */
    AI_CHAT_FORBIDDEN(15003, "没有权限访问该 AI 会话"),

    /** 模型调用失败。 */
    AI_CHAT_MODEL_FAILED(15004, "AI 模型调用失败，请稍后重试"),

    /** 模型配置不完整。 */
    AI_CHAT_MODEL_CONFIG_MISSING(15005, "AI 模型配置不完整");

    private final int code;
    private final String message;

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public int httpStatus() {
        return switch (this) {
            case AI_CONVERSATION_NOT_FOUND -> 404;
            case AI_CHAT_FORBIDDEN -> 403;
            case AI_CHAT_MODEL_FAILED, AI_CHAT_MODEL_CONFIG_MISSING -> 502;
            default -> 400;
        };
    }
}
