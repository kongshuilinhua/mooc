package com.elysia.mooc.ai.generator.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** AI 生成任务状态。 */
@Getter
@RequiredArgsConstructor
public enum AiGenerationStatus implements BaseEnum<String> {

    /** 待处理。 */
    PENDING("PENDING", "待处理"),

    /** 处理中。 */
    PROCESSING("PROCESSING", "处理中"),

    /** 生成成功。 */
    SUCCESS("SUCCESS", "生成成功"),

    /** 生成失败。 */
    FAILED("FAILED", "生成失败");

    @EnumValue
    private final String value;

    private final String desc;

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AiGenerationStatus of(Object value) {
        return BaseEnum.parse(AiGenerationStatus.class, normalize(value));
    }

    private static Object normalize(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        if ("RUNNING".equalsIgnoreCase(text)) {
            return PROCESSING.value;
        }
        if ("FAIL".equalsIgnoreCase(text)) {
            return FAILED.value;
        }
        return value;
    }
}
