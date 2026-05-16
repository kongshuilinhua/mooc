package com.elysia.mooc.ai.admin.domain.vo;

import lombok.Data;

/** AI 调用统计展示对象。 */
@Data
public class AiUsageVO {

    /** AI 消息总数。 */
    private Long messageCount;

    /** 用户消息数。 */
    private Long userMessageCount;

    /** 助手消息数。 */
    private Long assistantMessageCount;

    /** 成功消息数。 */
    private Long successMessageCount;

    /** 失败消息数。 */
    private Long failedMessageCount;

    /** Prompt token 总数。 */
    private Long promptTokens;

    /** Completion token 总数。 */
    private Long completionTokens;

    /** Token 总数。 */
    private Long totalTokens;

    /** Tool 调用总数。 */
    private Long toolCallCount;

    /** Tool 成功次数。 */
    private Long toolSuccessCount;

    /** Tool 失败次数。 */
    private Long toolFailedCount;

    /** Tool 平均耗时，单位毫秒。 */
    private Double averageToolCostMs;

    /** 知识库文档总数。 */
    private Long documentCount;

    /** 文档解析成功数。 */
    private Long parsedDocumentCount;

    /** 文档向量化成功数。 */
    private Long embeddedDocumentCount;
}
