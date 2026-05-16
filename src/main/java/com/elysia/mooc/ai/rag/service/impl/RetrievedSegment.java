package com.elysia.mooc.ai.rag.service.impl;

/** RAG 检索命中的知识切片。 */
public record RetrievedSegment(
        /** 知识库 ID。 */
        Long kbId,
        /** 文档 ID。 */
        Long documentId,
        /** 切片 ID。 */
        Long segmentId,
        /** 课程 ID。 */
        Long courseId,
        /** 来源类型。 */
        String sourceType,
        /** 标题。 */
        String title,
        /** 正文内容。 */
        String content,
        /** 相似度分数。 */
        Double score,
        /** 内容预览。 */
        String preview) {
}
