package com.elysia.mooc.ai.admin.domain.vo;

import com.elysia.mooc.knowledge.domain.enums.KnowledgeProcessStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 文档处理状态数量。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatusCountVO {

    /** 状态编码。 */
    private KnowledgeProcessStatus status;

    /** 状态中文说明。 */
    private String desc;

    /** 文档数量。 */
    private Long count;
}
