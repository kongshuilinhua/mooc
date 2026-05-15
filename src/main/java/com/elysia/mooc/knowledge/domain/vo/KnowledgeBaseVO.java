package com.elysia.mooc.knowledge.domain.vo;

import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeScopeType;
import java.time.LocalDateTime;
import lombok.Data;

/** 知识库响应对象。 */
@Data
public class KnowledgeBaseVO {

    /** 知识库 ID。 */
    private Long id;

    /** 知识库名称。 */
    private String name;

    /** 知识库编码。 */
    private String code;

    /** 知识库说明。 */
    private String description;

    /** 范围类型。 */
    private KnowledgeScopeType scopeType;

    /** 兼容早期前端展示字段。 */
    private String scope;

    /** 绑定课程 ID。 */
    private Long courseId;

    /** 启停状态。 */
    private EnableStatus status;

    /** 文档数量。 */
    private Long documentCount;

    /** 切片数量。 */
    private Long segmentCount;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
