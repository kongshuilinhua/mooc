package com.elysia.mooc.knowledge.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeProcessStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 文档切片分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeSegmentQuery extends PageQuery {

    /** 内容关键字。 */
    private String keyword;

    /** 向量化状态筛选。 */
    private KnowledgeProcessStatus status;

    /** 排序字段白名单：id、segmentIndex、createTime。 */
    private String sortBy = "segmentIndex";

    /** 是否升序。 */
    private Boolean isAsc = true;
}
