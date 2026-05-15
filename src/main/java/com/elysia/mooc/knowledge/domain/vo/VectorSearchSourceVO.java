package com.elysia.mooc.knowledge.domain.vo;

import com.elysia.mooc.knowledge.qdrant.QdrantPointPayload;
import lombok.Data;

/** 向量检索命中的来源片段。 */
@Data
public class VectorSearchSourceVO {

    /** Qdrant point ID。 */
    private String vectorId;

    /** 知识库 ID。 */
    private Long kbId;

    /** 文档 ID。 */
    private Long documentId;

    /** 切片 ID。 */
    private Long segmentId;

    /** 课程 ID，可为空。 */
    private Long courseId;

    /** 切片标题。 */
    private String title;

    /** 来源类型。 */
    private String sourceType;

    /** 相似度分数。 */
    private Double score;

    /** 内容摘要。 */
    private String contentPreview;

    /** 原始 payload，方便后台调试。 */
    private QdrantPointPayload payload;
}
