package com.elysia.mooc.ai.generator.domain.vo;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

/** AI 生成引用来源。 */
@Data
@Builder
public class GenerationSourceVO {

    /** 来源标题。 */
    private String title;

    /** 来源类型。 */
    private String sourceType;

    /** 来源业务 ID。 */
    private Long sourceId;

    /** 相似度或相关度。 */
    private BigDecimal similarity;

    /** 来源摘要片段。 */
    private String snippet;
}
