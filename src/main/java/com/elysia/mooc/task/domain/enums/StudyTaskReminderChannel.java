package com.elysia.mooc.task.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 学习任务提醒渠道。 */
@Getter
@RequiredArgsConstructor
public enum StudyTaskReminderChannel implements BaseEnum<String> {

    /** 站内信，前端使用 SITE_MESSAGE，SQL 种子兼容 MESSAGE。 */
    SITE_MESSAGE("SITE_MESSAGE", "站内信"),

    /** 邮件。 */
    EMAIL("EMAIL", "邮件"),

    /** 短信。 */
    SMS("SMS", "短信");

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
    public static StudyTaskReminderChannel of(Object value) {
        if (value != null && "MESSAGE".equalsIgnoreCase(String.valueOf(value).trim())) {
            return SITE_MESSAGE;
        }
        return BaseEnum.parse(StudyTaskReminderChannel.class, value);
    }
}
