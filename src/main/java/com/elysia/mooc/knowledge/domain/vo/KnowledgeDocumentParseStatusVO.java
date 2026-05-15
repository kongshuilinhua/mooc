package com.elysia.mooc.knowledge.domain.vo;

import com.elysia.mooc.knowledge.domain.enums.KnowledgeProcessStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 文档解析状态响应对象。 */
@Data
public class KnowledgeDocumentParseStatusVO {

    /** 文档 ID。 */
    private Long documentId;

    /** 知识库 ID。 */
    private Long kbId;

    /** 文档标题。 */
    private String title;

    /** 解析状态。 */
    private KnowledgeProcessStatus parseStatus;

    /** 解析失败原因。 */
    private String parseError;

    /** 切片数量。 */
    private Integer segmentCount;

    /** 向量化状态。 */
    private KnowledgeProcessStatus embeddingStatus;

    /** 向量化失败原因。 */
    private String embeddingError;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
