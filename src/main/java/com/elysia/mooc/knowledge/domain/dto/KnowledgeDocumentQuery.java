package com.elysia.mooc.knowledge.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeProcessStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 知识库文档分页查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeDocumentQuery extends PageQuery {

    /** 知识库 ID。 */
    private Long kbId;

    /** 兼容前端早期字段：知识库 ID。 */
    private Long knowledgeBaseId;

    /** 搜索关键字，匹配文档标题。 */
    private String keyword;

    /** 解析状态。 */
    private KnowledgeProcessStatus parseStatus;

    /** 向量化状态。 */
    private KnowledgeProcessStatus embeddingStatus;

    /** 排序字段白名单：id、createTime、updateTime。 */
    private String sortBy;

    /** 是否升序。 */
    private Boolean isAsc;

    public Long resolvedKbId() {
        return kbId == null ? knowledgeBaseId : kbId;
    }
}
