package com.elysia.mooc.interaction.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.interaction.domain.enums.InteractionTargetType;
import java.time.LocalDateTime;
import lombok.Data;

/** 互动点赞实体，映射 interaction_like 表。 */
@Data
@TableName("interaction_like")
public class InteractionLikePO {

    /** 点赞 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 目标类型。 */
    private InteractionTargetType targetType;

    /** 目标 ID。 */
    private Long targetId;

    /** 用户 ID。 */
    private Long userId;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 逻辑删除标记。 */
    private Integer deleted;
}
