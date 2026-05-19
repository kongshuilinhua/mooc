package com.elysia.mooc.ops.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 审核任务优先级。 */
@Getter
@RequiredArgsConstructor
public enum OpsReviewPriority implements BaseEnum<String> {

    /** 低优先级。 */
    LOW("LOW", "低"),

    /** 中优先级。 */
    MEDIUM("MEDIUM", "中"),

    /** 高优先级。 */
    HIGH("HIGH", "高");

    @EnumValue
    private final String value;

    private final String desc;

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OpsReviewPriority of(Object value) {
        return BaseEnum.parse(OpsReviewPriority.class, value);
    }
}
