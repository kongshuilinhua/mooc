package com.elysia.mooc.task.constants;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;

/** 学习任务模块错误码。 */
@RequiredArgsConstructor
public enum StudyTaskErrorCode implements ErrorCode {

    /** 当前用户不能操作学习任务。 */
    STUDY_TASK_FORBIDDEN(30001, "没有权限操作学习任务"),

    /** 学习任务计划已存在。 */
    STUDY_TASK_PLAN_DUPLICATED(30002, "当天同一来源的学习任务已存在"),

    /** 学习任务实例不存在。 */
    STUDY_TASK_INSTANCE_NOT_FOUND(30003, "学习任务实例不存在"),

    /** 学习任务计划不存在。 */
    STUDY_TASK_PLAN_NOT_FOUND(30004, "学习任务计划不存在"),

    /** 学习任务参数不合法。 */
    STUDY_TASK_PARAM_INVALID(30005, "学习任务参数不合法"),

    /** 提醒派发参数不合法。 */
    STUDY_TASK_REMINDER_INVALID(30006, "提醒派发参数不合法");

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
            case STUDY_TASK_FORBIDDEN -> 403;
            case STUDY_TASK_PLAN_DUPLICATED -> 409;
            case STUDY_TASK_INSTANCE_NOT_FOUND, STUDY_TASK_PLAN_NOT_FOUND -> 404;
            case STUDY_TASK_PARAM_INVALID, STUDY_TASK_REMINDER_INVALID -> 400;
        };
    }
}
