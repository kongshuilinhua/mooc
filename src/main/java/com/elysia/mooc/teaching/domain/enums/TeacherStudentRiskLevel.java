package com.elysia.mooc.teaching.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 学员学习风险等级，落库和接口值统一使用业务编码。 */
@Getter
@RequiredArgsConstructor
public enum TeacherStudentRiskLevel implements BaseEnum<String> {

    /** 正常。 */
    NORMAL("NORMAL", "正常"),

    /** 需要关注。 */
    ATTENTION("ATTENTION", "需关注"),

    /** 高风险。 */
    RISK("RISK", "高风险");

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
    public static TeacherStudentRiskLevel of(Object value) {
        return BaseEnum.parse(TeacherStudentRiskLevel.class, value);
    }
}
