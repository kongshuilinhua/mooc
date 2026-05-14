package com.elysia.mooc.interaction.domain.vo;

import com.elysia.mooc.interaction.domain.enums.AnswerAcceptedStatus;
import com.elysia.mooc.interaction.domain.enums.AnswerStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/** 问题回答展示项。 */
@Data
@Builder
public class AnswerItemVO {

    /** 回答 ID。 */
    private Long id;

    /** 问题 ID。 */
    private Long questionId;

    /** 回答人 ID。 */
    private Long userId;

    /** 回答内容。 */
    private String content;

    /** 是否采纳。 */
    private AnswerAcceptedStatus accepted;

    /** 点赞数。 */
    private Integer likeCount;

    /** 回答状态。 */
    private AnswerStatus status;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
