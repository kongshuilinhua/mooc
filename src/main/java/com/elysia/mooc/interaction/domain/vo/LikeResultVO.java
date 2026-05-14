package com.elysia.mooc.interaction.domain.vo;

import com.elysia.mooc.interaction.domain.enums.InteractionTargetType;
import lombok.Builder;
import lombok.Data;

/** 点赞结果。 */
@Data
@Builder
public class LikeResultVO {

    /** 点赞目标类型。 */
    private InteractionTargetType targetType;

    /** 点赞目标 ID。 */
    private Long targetId;

    /** 当前点赞数。 */
    private Integer currentLikeCount;

    /** 当前用户是否已点赞。 */
    private Boolean liked;
}
