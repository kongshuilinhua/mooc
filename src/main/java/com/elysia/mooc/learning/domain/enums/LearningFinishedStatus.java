package com.elysia.mooc.learning.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 学习完成状态，数据库值为 0/1。 */
@Getter
@RequiredArgsConstructor
public enum LearningFinishedStatus implements BaseEnum<Integer> {

    /** 未完成。 */
    UNFINISHED(0, "未完成"),

    /** 已完成。 */
    FINISHED(1, "已完成");

    /** 落库和接口输出值。 */
    @EnumValue
    private final Integer value;

    /** 中文说明。 */
    private final String desc;

    @JsonValue
    @Override
    public Integer getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static LearningFinishedStatus of(Object value) {
        return BaseEnum.parse(LearningFinishedStatus.class, value);
    }

    /** 判断是否完成。 */
    public boolean isFinished() {
        return this == FINISHED;
    }

    /** 根据布尔值构造完成状态。 */
    public static LearningFinishedStatus fromBoolean(boolean finished) {
        return finished ? FINISHED : UNFINISHED;
    }
}
