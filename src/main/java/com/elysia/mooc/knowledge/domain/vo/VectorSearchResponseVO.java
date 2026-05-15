package com.elysia.mooc.knowledge.domain.vo;

import java.util.List;
import lombok.Data;

/** 向量检索调试响应。 */
@Data
public class VectorSearchResponseVO {

    /** 原始查询文本。 */
    private String query;

    /** 命中来源。 */
    private List<VectorSearchSourceVO> sources;
}
