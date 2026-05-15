package com.elysia.mooc.knowledge.constants;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 知识库模块错误码。 */
@Getter
@RequiredArgsConstructor
public enum KnowledgeErrorCode implements ErrorCode {

    /** 请求参数不正确。 */
    KNOWLEDGE_PARAM_INVALID(12001, "知识库参数不正确"),

    /** 知识库不存在。 */
    KNOWLEDGE_BASE_NOT_FOUND(12002, "知识库不存在"),

    /** 知识库编码或名称冲突。 */
    KNOWLEDGE_BASE_DUPLICATED(12003, "知识库已存在"),

    /** 文档不存在。 */
    KNOWLEDGE_DOCUMENT_NOT_FOUND(12004, "知识库文档不存在"),

    /** 文档内容重复。 */
    KNOWLEDGE_DOCUMENT_DUPLICATED(12005, "同一知识库内文档已存在"),

    /** 无权操作知识库。 */
    KNOWLEDGE_FORBIDDEN(12006, "没有权限操作知识库"),

    /** 文档状态不允许当前操作。 */
    KNOWLEDGE_STATUS_INVALID(12007, "知识库文档状态不允许当前操作");

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
            case KNOWLEDGE_BASE_NOT_FOUND, KNOWLEDGE_DOCUMENT_NOT_FOUND -> 404;
            case KNOWLEDGE_FORBIDDEN -> 403;
            case KNOWLEDGE_BASE_DUPLICATED, KNOWLEDGE_DOCUMENT_DUPLICATED, KNOWLEDGE_STATUS_INVALID -> 409;
            default -> 400;
        };
    }
}
