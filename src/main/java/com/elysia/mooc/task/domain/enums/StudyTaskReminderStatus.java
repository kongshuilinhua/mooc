package com.elysia.mooc.task.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 学习任务提醒发送状态。 */
@Getter
@RequiredArgsConstructor
public enum StudyTaskReminderStatus implements BaseEnum<String> {

    /** 待发送。 */
    PENDING("PENDING", "待发送"),

    /** 已发送。 */
    SENT("SENT", "已发送"),

    /** 已跳过。 */
    SKIPPED("SKIPPED", "已跳过"),

    /** 发送失败。 */
    FAILED("FAILED", "发送失败");

    /** 落库和接口输出值。 */
    @EnumValue
    private final String value;

    /** 中文说明。 */
    private final String desc;

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static StudyTaskReminderStatus of(Object value) {
        return BaseEnum.parse(StudyTaskReminderStatus.class, value);
    }
}
