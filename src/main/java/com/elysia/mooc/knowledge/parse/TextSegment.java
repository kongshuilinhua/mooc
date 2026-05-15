package com.elysia.mooc.knowledge.parse;

/** 文本切片结果。 */
public record TextSegment(
        Integer segmentIndex,
        String title,
        String content,
        Integer tokenCount,
        String metadata) {
}
