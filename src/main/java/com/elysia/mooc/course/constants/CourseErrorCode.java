package com.elysia.mooc.course.constants;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 课程模块错误码。 */
@Getter
@RequiredArgsConstructor
public enum CourseErrorCode implements ErrorCode {

    /** 课程参数错误。 */
    COURSE_PARAM_INVALID(4001, "课程参数不正确"),

    /** 课程分类不可用。 */
    COURSE_CATEGORY_INVALID(4002, "课程分类不可用"),

    /** 课程标签不可用。 */
    COURSE_TAG_INVALID(4003, "课程标签不可用"),

    /** 课程不存在或不可见。 */
    COURSE_NOT_FOUND(4004, "课程不存在或未发布"),

    /** 当前用户无课程操作权限。 */
    COURSE_FORBIDDEN(4005, "无权限操作该课程"),

    /** 当前课程状态不允许操作。 */
    COURSE_STATUS_INVALID(4006, "当前课程状态不允许操作");

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
}
