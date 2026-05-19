package com.elysia.mooc.ai.generator.constants;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** AI 生成模块错误码。 */
@Getter
@RequiredArgsConstructor
public enum AiGeneratorErrorCode implements ErrorCode {

    /** 请求参数不合法。 */
    AI_GENERATOR_PARAM_INVALID(28001, "AI 生成参数不合法"),

    /** 当前用户没有 AI 生成权限。 */
    AI_GENERATOR_FORBIDDEN(28002, "没有权限使用该 AI 生成功能"),

    /** 课程不存在。 */
    AI_GENERATOR_COURSE_NOT_FOUND(28003, "课程不存在或已删除"),

    /** 章节不存在。 */
    AI_GENERATOR_CHAPTER_NOT_FOUND(28004, "章节不存在或已删除"),

    /** 当前教师不能操作该课程。 */
    AI_GENERATOR_COURSE_FORBIDDEN(28005, "没有权限操作该课程的 AI 生成任务"),

    /** 学生不存在。 */
    AI_GENERATOR_STUDENT_NOT_FOUND(28006, "学生不存在或已删除"),

    /** 学习路径素材不足。 */
    AI_GENERATOR_LEARNING_DATA_NOT_ENOUGH(28007, "学习数据不足，暂不能生成有效学习路径"),

    /** 模型生成失败。 */
    AI_GENERATOR_MODEL_FAILED(28008, "AI 生成失败，请稍后重试");

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
            case AI_GENERATOR_FORBIDDEN, AI_GENERATOR_COURSE_FORBIDDEN -> 403;
            case AI_GENERATOR_COURSE_NOT_FOUND, AI_GENERATOR_CHAPTER_NOT_FOUND, AI_GENERATOR_STUDENT_NOT_FOUND -> 404;
            default -> 400;
        };
    }
}
