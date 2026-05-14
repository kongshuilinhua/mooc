package com.elysia.mooc.learning.constants;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 学习模块错误码。 */
@Getter
@RequiredArgsConstructor
public enum LearningErrorCode implements ErrorCode {

    /** 学习参数错误。 */
    LEARNING_PARAM_INVALID(8001, "学习参数不正确"),

    /** 课程不存在或不可学习。 */
    LEARNING_COURSE_NOT_AVAILABLE(8002, "课程不存在或暂不可学习"),

    /** 课程小节不存在或不属于该课程。 */
    LEARNING_SECTION_NOT_FOUND(8003, "课程小节不存在或不属于该课程"),

    /** 尚未加入课程。 */
    LEARNING_COURSE_NOT_JOINED(8004, "请先加入课程后再学习"),

    /** 学习记录不存在。 */
    LEARNING_RECORD_NOT_FOUND(8005, "学习记录不存在");

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
            case LEARNING_COURSE_NOT_AVAILABLE,
                    LEARNING_SECTION_NOT_FOUND,
                    LEARNING_RECORD_NOT_FOUND -> 404;
            case LEARNING_COURSE_NOT_JOINED -> 409;
            default -> 400;
        };
    }
}
