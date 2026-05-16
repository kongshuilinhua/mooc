package com.elysia.mooc.ai.rag.domain.vo;

import com.elysia.mooc.ai.chat.domain.vo.ChatResultVO;
import com.elysia.mooc.ai.chat.domain.vo.AiSourceVO;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** RAG 问答响应，主结构复用普通聊天响应。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RagChatResult extends ChatResultVO {

    /** 兼容 day16 文档旧字段，主字段仍使用 content。 */
    private String answer;

    /** 兼容引用字段命名，主字段仍使用 sources。 */
    private List<AiSourceVO> citations;
}
