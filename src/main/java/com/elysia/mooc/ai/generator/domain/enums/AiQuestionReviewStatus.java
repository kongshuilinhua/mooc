package com.elysia.mooc.ai.generator.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** AI 题目草稿审核状态。 */
@Getter
@RequiredArgsConstructor
public enum AiQuestionReviewStatus implements BaseEnum<String> {

    /** 待审核。 */
    PENDING("PENDING", "待审核"),

    /** 已通过。 */
    APPROVED("APPROVED", "已通过"),

    /** 已拒绝。 */
    REJECTED("REJECTED", "已拒绝");

    @EnumValue
    private final String value;

    private final String desc;

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AiQuestionReviewStatus of(Object value) {
        return BaseEnum.parse(AiQuestionReviewStatus.class, value);
    }
}
