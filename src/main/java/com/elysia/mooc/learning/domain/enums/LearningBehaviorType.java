package com.elysia.mooc.learning.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 学习行为类型。 */
@Getter
@RequiredArgsConstructor
public enum LearningBehaviorType implements BaseEnum<String> {

    /** 浏览课程。 */
    VIEW("VIEW", "浏览"),

    /** 开始播放。 */
    PLAY("PLAY", "播放"),

    /** 暂停播放。 */
    PAUSE("PAUSE", "暂停"),

    /** 学习心跳。 */
    HEARTBEAT("HEARTBEAT", "心跳"),

    /** 完成小节。 */
    FINISH("FINISH", "完成");

    /** 落库和接口输出值。 */
    @EnumValue
    private final String value;

    /** 中文说明。 */
    private final String desc;

    @JsonValue
    @Override
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static LearningBehaviorType of(Object value) {
        return BaseEnum.parse(LearningBehaviorType.class, value);
    }
}
