package com.elysia.mooc.ai.rag.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** RAG 问答请求。 */
@Data
public class RagChatRequest {

    /** 会话 ID，不传则创建 RAG 会话。 */
    @Min(value = 1, message = "会话ID必须为正数")
    private Long conversationId;

    /** 用户问题，day16 主字段。 */
    @Size(max = 4000, message = "问题内容不能超过4000个字符")
    private String message;

    /** 兼容 day16 早期文档字段，服务端会统一收敛到 message。 */
    @Size(max = 4000, message = "问题内容不能超过4000个字符")
    private String question;

    /** 指定知识库 ID，可为空。 */
    @Min(value = 1, message = "知识库ID必须为正数")
    private Long knowledgeBaseId;

    /** 课程上下文 ID，可为空。 */
    @Min(value = 1, message = "课程ID必须为正数")
    private Long courseId;

    /** 检索数量。 */
    @Min(value = 1, message = "检索数量不能小于1")
    @Max(value = 20, message = "检索数量不能大于20")
    private Integer topK;
}
