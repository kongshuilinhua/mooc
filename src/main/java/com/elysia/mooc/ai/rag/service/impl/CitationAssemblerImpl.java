package com.elysia.mooc.ai.rag.service.impl;

import com.elysia.mooc.ai.chat.domain.vo.AiSourceVO;
import com.elysia.mooc.ai.rag.service.CitationAssembler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** RAG 引用来源组装实现。 */
@Component
@RequiredArgsConstructor
public class CitationAssemblerImpl implements CitationAssembler {

    private final ObjectMapper objectMapper;

    /**
     * 构造引用来源。
     *
     * @param segments 命中切片
     * @return 引用来源
     */
    @Override
    public List<AiSourceVO> buildCitations(List<RetrievedSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return List.of();
        }
        Map<String, AiSourceVO> result = new LinkedHashMap<>();
        for (RetrievedSegment segment : segments) {
            String key = segment.kbId() + ":" + segment.documentId() + ":" + segment.segmentId();
            result.putIfAbsent(key, toSource(segment));
        }
        return List.copyOf(result.values());
    }

    /**
     * 序列化引用 JSON。
     *
     * @param citations 引用来源
     * @return JSON 字符串
     */
    @Override
    public String toJson(List<AiSourceVO> citations) {
        if (citations == null || citations.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(citations);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }

    private AiSourceVO toSource(RetrievedSegment segment) {
        AiSourceVO vo = new AiSourceVO();
        vo.setSourceType(segment.sourceType());
        vo.setSourceId(segment.segmentId());
        vo.setKbId(segment.kbId());
        vo.setDocumentId(segment.documentId());
        vo.setSegmentId(segment.segmentId());
        vo.setCourseId(segment.courseId());
        vo.setTitle(segment.title());
        vo.setScore(segment.score());
        vo.setPreview(segment.preview());
        return vo;
    }
}
