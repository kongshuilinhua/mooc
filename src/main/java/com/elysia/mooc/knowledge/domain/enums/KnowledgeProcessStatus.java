package com.elysia.mooc.knowledge.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 知识库文档处理状态，用于解析和向量化两个阶段。 */
@Getter
@RequiredArgsConstructor
public enum KnowledgeProcessStatus implements BaseEnum<String> {

    /** 待处理。 */
    PENDING("PENDING", "待处理"),

    /** 处理中。 */
    PROCESSING("PROCESSING", "处理中"),

    /** 处理成功。 */
    SUCCESS("SUCCESS", "成功"),

    /** 处理失败。 */
    FAILED("FAILED", "失败");

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
    public static KnowledgeProcessStatus of(Object value) {
        return BaseEnum.parse(KnowledgeProcessStatus.class, normalizeNumber(value));
    }

    private static Object normalizeNumber(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return switch (text) {
            case "0" -> PENDING.value;
            case "1" -> PROCESSING.value;
            case "2" -> SUCCESS.value;
            case "3" -> FAILED.value;
            default -> value;
        };
    }
}
