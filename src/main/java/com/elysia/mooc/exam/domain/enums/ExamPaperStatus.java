package com.elysia.mooc.exam.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 试卷状态。 */
@Getter
@RequiredArgsConstructor
public enum ExamPaperStatus implements BaseEnum<String> {

    /** 草稿。 */
    DRAFT("DRAFT", "草稿"),

    /** 已发布。 */
    PUBLISHED("PUBLISHED", "已发布"),

    /** 已下线。 */
    OFFLINE("OFFLINE", "已下线");

    @EnumValue
    private final String value;

    private final String desc;

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ExamPaperStatus of(Object value) {
        return BaseEnum.parse(ExamPaperStatus.class, value);
    }
}
