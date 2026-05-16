package com.elysia.mooc.ai.rag.domain.vo;

import com.elysia.mooc.ai.chat.domain.vo.AiSourceVO;
import java.util.List;
import lombok.Data;

/** RAG 检索预览响应。 */
@Data
public class RagSearchResult {

    /** 本次检索文本。 */
    private String query;

    /** 命中的引用片段。 */
    private List<AiSourceVO> sources;

    /** 兼容前端统一消息结构，无命中时返回固定提示。 */
    private String content;

    /** 兼容 day16 文档旧字段。 */
    private String answer;
}
