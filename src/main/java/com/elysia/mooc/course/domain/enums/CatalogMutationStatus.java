package com.elysia.mooc.course.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 课程目录变更状态。 */
@Getter
@RequiredArgsConstructor
public enum CatalogMutationStatus implements BaseEnum<String> {

    /** 已创建。 */
    CREATED("CREATED", "已创建"),

    /** 已更新。 */
    UPDATED("UPDATED", "已更新"),

    /** 已删除。 */
    DELETED("DELETED", "已删除");

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
    public static CatalogMutationStatus of(Object value) {
        return BaseEnum.parse(CatalogMutationStatus.class, value);
    }
}
