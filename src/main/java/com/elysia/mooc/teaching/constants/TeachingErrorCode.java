package com.elysia.mooc.teaching.constants;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 教师看板模块错误码。 */
@Getter
@RequiredArgsConstructor
public enum TeachingErrorCode implements ErrorCode {

    /** 当前用户不是教师，不能访问教师看板。 */
    TEACHING_FORBIDDEN(27001, "没有权限访问教师看板"),

    /** 课程不存在或已删除。 */
    TEACHING_COURSE_NOT_FOUND(27002, "课程不存在或已删除"),

    /** 当前教师不能访问该课程。 */
    TEACHING_COURSE_FORBIDDEN(27003, "没有权限查看该课程的教学数据"),

    /** 日期范围不合法。 */
    TEACHING_DATE_RANGE_INVALID(27004, "开始日期不能晚于结束日期");

    private final int code;
    private final String message;

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public int httpStatus() {
        return switch (this) {
            case TEACHING_FORBIDDEN, TEACHING_COURSE_FORBIDDEN -> 403;
            case TEACHING_COURSE_NOT_FOUND -> 404;
            case TEACHING_DATE_RANGE_INVALID -> 400;
        };
    }
}
