package com.elysia.mooc.interaction.domain.dto;

import com.elysia.mooc.interaction.domain.enums.InteractionTargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** 点赞请求参数。 */
@Data
public class LikeRequest {

    /** 点赞目标类型。 */
    @NotNull(message = "点赞对象类型不能为空")
    private InteractionTargetType targetType;

    /** 点赞目标 ID。 */
    @NotNull(message = "点赞对象ID不能为空")
    @Positive(message = "点赞对象ID必须为正数")
    private Long targetId;

    /** 前端兼容字段，本轮点赞接口按幂等“确保已点赞”处理。 */
    private Boolean liked;

    /** 前端兼容字段，本轮点赞接口按幂等“确保已点赞”处理。 */
    private Boolean isLiked;
}
