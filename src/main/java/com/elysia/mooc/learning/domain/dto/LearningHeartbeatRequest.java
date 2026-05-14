package com.elysia.mooc.learning.domain.dto;

import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import com.elysia.mooc.learning.constants.LearningErrorCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** 学习心跳请求。 */
@Data
public class LearningHeartbeatRequest implements Checker {

    /** 课程 ID。 */
    @NotNull(message = "课程ID不能为空")
    @Positive(message = "课程ID必须为正数")
    private Long courseId;

    /** 小节 ID。 */
    @NotNull(message = "小节ID不能为空")
    @Positive(message = "小节ID必须为正数")
    private Long sectionId;

    /** 当前播放位置，单位秒。 */
    @NotNull(message = "播放位置不能为空")
    @Min(value = 0, message = "播放位置不能小于0")
    private Integer position;

    /** 小节总时长，单位秒。 */
    @NotNull(message = "视频时长不能为空")
    @Min(value = 0, message = "视频时长不能小于0")
    private Integer duration;

    /**
     * 校验跨字段业务规则。
     */
    @Override
    public void check() {
        // position 大于 duration 会把课程进度直接刷满，必须在入库前拦截。
        if (position != null && duration != null && duration > 0 && position > duration) {
            throw new BizException(LearningErrorCode.LEARNING_PARAM_INVALID, "播放位置不能超过视频总时长");
        }
    }
}
