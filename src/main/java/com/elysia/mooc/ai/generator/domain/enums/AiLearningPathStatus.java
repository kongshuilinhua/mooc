package com.elysia.mooc.ai.generator.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** AI 学习路径状态。 */
@Getter
@RequiredArgsConstructor
public enum AiLearningPathStatus implements BaseEnum<String> {

    /** 生效中。 */
    ACTIVE("ACTIVE", "生效中"),

    /** 已完成。 */
    COMPLETED("COMPLETED", "已完成"),

    /** 已失效。 */
    EXPIRED("EXPIRED", "已失效"),

    /** 已停用。 */
    DISABLED("DISABLED", "已停用");

    @EnumValue
    private final String value;

    private final String desc;

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AiLearningPathStatus of(Object value) {
        return BaseEnum.parse(AiLearningPathStatus.class, value);
    }
}
