package com.elysia.mooc.interaction.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 举报处理状态。 */
@Getter
@RequiredArgsConstructor
public enum ReportStatus implements BaseEnum<String> {

    /** 待处理。 */
    PENDING("PENDING", "待处理"),

    /** 已处理。 */
    RESOLVED("RESOLVED", "已处理"),

    /** 已驳回。 */
    REJECTED("REJECTED", "已驳回");

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
    public static ReportStatus of(Object value) {
        return BaseEnum.parse(ReportStatus.class, value);
    }
}
