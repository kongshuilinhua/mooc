package com.elysia.mooc.ai.rag.constants;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** RAG 问答模块错误码。 */
@Getter
@RequiredArgsConstructor
public enum RagErrorCode implements ErrorCode {

    /** 请求参数不正确。 */
    RAG_PARAM_INVALID(16001, "RAG 参数不正确"),

    /** 无权访问 RAG 会话或知识库。 */
    RAG_FORBIDDEN(16002, "没有权限访问该 RAG 资源"),

    /** 向量检索失败。 */
    RAG_RETRIEVAL_FAILED(16003, "知识库检索服务暂不可用"),

    /** 模型生成失败。 */
    RAG_MODEL_FAILED(16004, "AI 模型调用失败，请稍后重试");

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
            case RAG_FORBIDDEN -> 403;
            case RAG_RETRIEVAL_FAILED, RAG_MODEL_FAILED -> 502;
            default -> 400;
        };
    }
}
