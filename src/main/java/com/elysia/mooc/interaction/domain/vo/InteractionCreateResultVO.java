package com.elysia.mooc.interaction.domain.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/** 互动创建结果。 */
@Data
@Builder
public class InteractionCreateResultVO {

    /** 统一主键 ID。 */
    private Long id;

    /** 创建问题时返回的问题 ID。 */
    private Long questionId;

    /** 创建回答时返回的回答 ID。 */
    private Long answerId;

    /** 创建后的状态。 */
    private String status;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
