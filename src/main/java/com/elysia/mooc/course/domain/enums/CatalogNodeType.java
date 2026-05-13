package com.elysia.mooc.course.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 课程目录节点类型。 */
@Getter
@RequiredArgsConstructor
public enum CatalogNodeType implements BaseEnum<String> {

    /** 章节。 */
    CHAPTER("CHAPTER", "章节"),

    /** 小节。 */
    SECTION("SECTION", "小节"),

    /** 知识点。 */
    CONCEPT("CONCEPT", "知识点");

    /** 接口输出值。 */
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
    public static CatalogNodeType of(Object value) {
        return BaseEnum.parse(CatalogNodeType.class, value);
    }
}
