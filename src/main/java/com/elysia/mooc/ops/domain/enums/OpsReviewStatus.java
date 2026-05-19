package com.elysia.mooc.ops.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 审核任务状态。 */
@Getter
@RequiredArgsConstructor
public enum OpsReviewStatus implements BaseEnum<String> {

    /** 待审核。 */
    PENDING("PENDING", "待审核"),

    /** 审核通过。 */
    APPROVED("APPROVED", "审核通过"),

    /** 审核拒绝。 */
    REJECTED("REJECTED", "审核拒绝");

    @EnumValue
    private final String value;

    private final String desc;

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OpsReviewStatus of(Object value) {
        return BaseEnum.parse(OpsReviewStatus.class, value);
    }
}
