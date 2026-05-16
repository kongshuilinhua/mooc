package com.elysia.mooc.exam.constants;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 考试模块错误码。 */
@Getter
@RequiredArgsConstructor
public enum ExamErrorCode implements ErrorCode {

    /** 考试参数不正确。 */
    EXAM_PARAM_INVALID(20001, "考试参数不正确"),

    /** 当前用户无考试维护权限。 */
    EXAM_FORBIDDEN(20002, "无权限操作考试资源"),

    /** 题目不存在或不可用。 */
    QUESTION_NOT_FOUND(20003, "题目不存在或不可用"),

    /** 试卷不存在或不可用。 */
    PAPER_NOT_FOUND(20004, "试卷不存在或不可用"),

    /** 当前试卷状态不允许作答。 */
    PAPER_STATUS_INVALID(20005, "当前试卷状态不允许作答"),

    /** 试卷题目不完整。 */
    PAPER_QUESTION_INVALID(20006, "试卷题目不完整"),

    /** 课程不存在或不可用。 */
    COURSE_NOT_FOUND(20007, "课程不存在或不可用");

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
     * 按接口合同返回 HTTP 状态，确保权限和状态冲突不会被包装成 200。
     *
     * @return HTTP 状态码
     */
    @Override
    public int httpStatus() {
        return switch (this) {
            case EXAM_FORBIDDEN -> 403;
            case QUESTION_NOT_FOUND, PAPER_NOT_FOUND, COURSE_NOT_FOUND -> 404;
            case PAPER_STATUS_INVALID, PAPER_QUESTION_INVALID -> 409;
            default -> 400;
        };
    }
}
