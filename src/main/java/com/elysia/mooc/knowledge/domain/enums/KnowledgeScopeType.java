package com.elysia.mooc.knowledge.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 知识库范围类型，决定知识库面向平台、课程还是运营后台。 */
@Getter
@RequiredArgsConstructor
public enum KnowledgeScopeType implements BaseEnum<String> {

    /** 平台全局知识库。 */
    GLOBAL("GLOBAL", "平台"),

    /** 课程专属知识库。 */
    COURSE("COURSE", "课程"),

    /** 运营后台知识库。 */
    ADMIN("ADMIN", "运营");

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
    public static KnowledgeScopeType of(Object value) {
        return BaseEnum.parse(KnowledgeScopeType.class, normalizeLegacy(value));
    }

    private static Object normalizeLegacy(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        if ("platform".equalsIgnoreCase(text)) {
            return GLOBAL.value;
        }
        if ("course".equalsIgnoreCase(text)) {
            return COURSE.value;
        }
        if ("private".equalsIgnoreCase(text)) {
            return ADMIN.value;
        }
        return value;
    }
}
