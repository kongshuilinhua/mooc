package com.elysia.mooc.ops.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 后台导出类型白名单。 */
@Getter
@RequiredArgsConstructor
public enum OpsExportType implements BaseEnum<String> {

    /** 课程审核导出。 */
    COURSE_AUDIT("COURSE_AUDIT", "课程审核导出"),

    /** 学习报告导出。 */
    LEARNING_REPORT("LEARNING_REPORT", "学习报告导出"),

    /** AI 使用统计导出。 */
    AI_USAGE("AI_USAGE", "AI 使用统计导出"),

    /** 运营审计导出。 */
    OPS_AUDIT("OPS_AUDIT", "运营审计导出");

    @EnumValue
    private final String value;

    private final String desc;

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OpsExportType of(Object value) {
        return BaseEnum.parse(OpsExportType.class, value);
    }
}
