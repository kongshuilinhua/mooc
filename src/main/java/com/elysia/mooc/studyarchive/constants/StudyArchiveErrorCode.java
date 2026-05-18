package com.elysia.mooc.studyarchive.constants;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;

/** 学习档案模块错误码。 */
@RequiredArgsConstructor
public enum StudyArchiveErrorCode implements ErrorCode {

    /** 当前用户不能访问学习档案。 */
    STUDY_ARCHIVE_FORBIDDEN(26001, "没有权限访问学习档案"),

    /** 课程不存在。 */
    STUDY_ARCHIVE_COURSE_NOT_FOUND(26002, "课程不存在"),

    /** 小节不存在或不属于当前课程。 */
    STUDY_ARCHIVE_SECTION_INVALID(26003, "小节不存在或不属于当前课程"),

    /** 学生未加入课程。 */
    STUDY_ARCHIVE_COURSE_NOT_JOINED(26004, "请先加入课程后再保存学习笔记"),

    /** 学习报告日期不合法。 */
    STUDY_ARCHIVE_REPORT_DATE_INVALID(26005, "学习报告日期不合法");

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
            case STUDY_ARCHIVE_FORBIDDEN -> 403;
            case STUDY_ARCHIVE_COURSE_NOT_FOUND, STUDY_ARCHIVE_SECTION_INVALID -> 404;
            case STUDY_ARCHIVE_COURSE_NOT_JOINED -> 409;
            case STUDY_ARCHIVE_REPORT_DATE_INVALID -> 400;
        };
    }
}
