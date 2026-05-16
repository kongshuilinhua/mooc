package com.elysia.mooc.ai.rag.service.impl;

import com.elysia.mooc.ai.rag.service.RagPromptBuilder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** RAG Prompt 构造实现。 */
@Component
@RequiredArgsConstructor
public class RagPromptBuilderImpl implements RagPromptBuilder {

    private final RagProperties ragProperties;

    /**
     * 构造 RAG Prompt。
     *
     * @param question 用户问题
     * @param segments 检索切片
     * @return Prompt 文本
     */
    @Override
    public String build(String question, List<RetrievedSegment> segments) {
        StringBuilder builder = new StringBuilder(2048);
        builder.append("你是 MOOC 平台的 RAG 学习助手。请严格基于给定资料回答用户问题。").append('\n');
        builder.append("要求：").append('\n');
        builder.append("1. 只使用资料中的信息回答，不要编造课程不存在的信息。").append('\n');
        builder.append("2. 如果资料不足以回答，请直接说明资料不足。").append('\n');
        builder.append("3. 回答要使用中文，准确、简洁，适合学习场景。").append('\n');
        builder.append("4. 可在正文中使用 [来源1] 这样的编号提示依据。").append('\n').append('\n');
        builder.append("用户问题：").append(question).append('\n').append('\n');
        builder.append("资料片段：").append('\n');
        int index = 1;
        for (RetrievedSegment segment : segments) {
            builder.append("[来源").append(index++).append("] ");
            if (StringUtils.hasText(segment.title())) {
                builder.append("标题：").append(segment.title()).append('\n');
            }
            builder.append("知识库ID：").append(segment.kbId())
                    .append("，文档ID：").append(segment.documentId())
                    .append("，切片ID：").append(segment.segmentId())
                    .append("，相似度：").append(String.format("%.4f", segment.score()))
                    .append('\n');
            builder.append(trimSegment(segment.content())).append('\n').append('\n');
        }
        builder.append("请输出最终回答：");
        return builder.toString();
    }

    private String trimSegment(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String text = content.trim();
        int maxLength = Math.max(100, ragProperties.getMaxSegmentChars());
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
}
