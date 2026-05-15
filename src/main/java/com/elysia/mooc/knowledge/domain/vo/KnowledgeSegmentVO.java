package com.elysia.mooc.knowledge.domain.vo;

import com.elysia.mooc.knowledge.domain.enums.KnowledgeProcessStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 知识切片响应对象，供后续解析和向量化模块复用。 */
@Data
public class KnowledgeSegmentVO {

    /** 切片 ID。 */
    private Long id;

    /** 知识库 ID。 */
    private Long kbId;

    /** 文档 ID。 */
    private Long documentId;

    /** 切片序号。 */
    private Integer segmentIndex;

    /** 切片标题。 */
    private String title;

    /** 切片内容。 */
    private String content;

    /** token 数量。 */
    private Integer tokenCount;

    /** JSON 扩展元数据。 */
    private String metadata;

    /** Qdrant 向量 ID。 */
    private String vectorId;

    /** 向量化状态。 */
    private KnowledgeProcessStatus embeddingStatus;

    /** 向量化错误。 */
    private String embeddingError;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
