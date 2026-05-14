package com.elysia.mooc.course.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 课程审核动作。 */
@Getter
@RequiredArgsConstructor
public enum CourseAuditAction implements BaseEnum<String> {

    /** 教师提交审核。 */
    SUBMIT("SUBMIT", "提交审核"),

    /** 管理员审核通过。 */
    APPROVE("APPROVE", "审核通过"),

    /** 管理员审核驳回。 */
    REJECT("REJECT", "审核驳回"),

    /** 管理员下架课程。 */
    OFFLINE("OFFLINE", "课程下架");

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
    public static CourseAuditAction of(Object value) {
        return BaseEnum.parse(CourseAuditAction.class, value);
    }
}
