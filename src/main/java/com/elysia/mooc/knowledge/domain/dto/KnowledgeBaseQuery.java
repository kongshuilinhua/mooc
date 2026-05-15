package com.elysia.mooc.knowledge.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.common.enums.EnableStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 知识库分页查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeBaseQuery extends PageQuery {

    /** 搜索关键字，匹配名称或编码。 */
    private String keyword;

    /** 启停状态。 */
    private EnableStatus status;

    /** 排序字段白名单：id、createTime、updateTime。 */
    private String sortBy;

    /** 是否升序。 */
    private Boolean isAsc;
}
