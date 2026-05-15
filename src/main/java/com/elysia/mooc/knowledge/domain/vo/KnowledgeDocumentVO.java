package com.elysia.mooc.knowledge.domain.vo;

import com.elysia.mooc.knowledge.domain.enums.KnowledgeDocumentSourceType;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeProcessStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 知识库文档响应对象。 */
@Data
public class KnowledgeDocumentVO {

    /** 文档 ID。 */
    private Long id;

    /** 知识库 ID。 */
    private Long kbId;

    /** 兼容前端早期字段：知识库 ID。 */
    private Long knowledgeBaseId;

    /** 知识库名称。 */
    private String knowledgeBaseName;

    /** 媒资文件 ID。 */
    private Long mediaFileId;

    /** 文档标题。 */
    private String title;

    /** 来源类型。 */
    private KnowledgeDocumentSourceType sourceType;

    /** 来源地址。 */
    private String sourceUrl;

    /** 内容摘要。 */
    private String contentHash;

    /** 解析状态。 */
    private KnowledgeProcessStatus parseStatus;

    /** 解析错误。 */
    private String parseError;

    /** 切片数量。 */
    private Integer segmentCount;

    /** 向量化状态。 */
    private KnowledgeProcessStatus embeddingStatus;

    /** 向量化错误。 */
    private String embeddingError;

    /** 兼容前端早期错误字段。 */
    private String errorMessage;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
