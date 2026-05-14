package com.elysia.mooc.interaction.constants;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 互动模块错误码。 */
@Getter
@RequiredArgsConstructor
public enum InteractionErrorCode implements ErrorCode {

    /** 互动参数错误。 */
    INTERACTION_PARAM_INVALID(9001, "互动参数不正确"),

    /** 课程不存在或不可互动。 */
    INTERACTION_COURSE_NOT_AVAILABLE(9002, "课程不存在或暂不可互动"),

    /** 问题不存在或不可见。 */
    INTERACTION_QUESTION_NOT_FOUND(9003, "问题不存在或不可见"),

    /** 回答不存在或不可见。 */
    INTERACTION_ANSWER_NOT_FOUND(9004, "回答不存在或不可见"),

    /** 当前用户无互动操作权限。 */
    INTERACTION_FORBIDDEN(9005, "无权限操作该互动内容"),

    /** 当前互动状态不允许操作。 */
    INTERACTION_STATUS_INVALID(9006, "当前互动状态不允许操作"),

    /** 举报不存在。 */
    INTERACTION_REPORT_NOT_FOUND(9007, "举报记录不存在");

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
            case INTERACTION_COURSE_NOT_AVAILABLE,
                    INTERACTION_QUESTION_NOT_FOUND,
                    INTERACTION_ANSWER_NOT_FOUND,
                    INTERACTION_REPORT_NOT_FOUND -> 404;
            case INTERACTION_FORBIDDEN -> 403;
            case INTERACTION_STATUS_INVALID -> 409;
            default -> 400;
        };
    }
}
