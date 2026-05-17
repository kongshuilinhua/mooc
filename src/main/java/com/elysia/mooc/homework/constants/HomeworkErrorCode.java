package com.elysia.mooc.homework.constants;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 作业模块错误码。 */
@Getter
@RequiredArgsConstructor
public enum HomeworkErrorCode implements ErrorCode {

    /** 作业参数不正确。 */
    HOMEWORK_PARAM_INVALID(25001, "作业参数不正确"),

    /** 当前用户无作业操作权限。 */
    HOMEWORK_FORBIDDEN(25002, "无权限操作作业资源"),

    /** 课程不存在或不可用。 */
    HOMEWORK_COURSE_NOT_FOUND(25003, "课程不存在或不可用"),

    /** 章节不存在或不属于该课程。 */
    HOMEWORK_CHAPTER_INVALID(25004, "章节不存在或不属于该课程"),

    /** 作业不存在或不可用。 */
    HOMEWORK_ASSIGNMENT_NOT_FOUND(25005, "作业不存在或不可用"),

    /** 作业状态不允许当前操作。 */
    HOMEWORK_STATUS_INVALID(25006, "当前作业状态不允许提交"),

    /** 作业已截止。 */
    HOMEWORK_DEADLINE_EXPIRED(25007, "作业已截止，不能提交"),

    /** 当前学生未加入课程。 */
    HOMEWORK_COURSE_NOT_JOINED(25008, "请先加入课程后再提交作业"),

    /** 当前作业不允许重复提交。 */
    HOMEWORK_SUBMIT_DUPLICATED(25009, "当前作业不允许重复提交"),

    /** 提交记录不存在或不可用。 */
    HOMEWORK_SUBMISSION_NOT_FOUND(25010, "作业提交记录不存在或不可用");

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

    /**
     * 按接口合同返回真实 HTTP 状态，避免业务错误被包装成 200。
     *
     * @return HTTP 状态码
     */
    @Override
    public int httpStatus() {
        return switch (this) {
            case HOMEWORK_FORBIDDEN -> 403;
            case HOMEWORK_COURSE_NOT_FOUND, HOMEWORK_ASSIGNMENT_NOT_FOUND, HOMEWORK_SUBMISSION_NOT_FOUND -> 404;
            case HOMEWORK_STATUS_INVALID, HOMEWORK_DEADLINE_EXPIRED, HOMEWORK_COURSE_NOT_JOINED,
                    HOMEWORK_SUBMIT_DUPLICATED -> 409;
            default -> 400;
        };
    }
}
