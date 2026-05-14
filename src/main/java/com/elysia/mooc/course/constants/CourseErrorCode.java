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
    COURSE_STATUS_INVALID(4006, "当前课程状态不允许操作"),

    /** 课程发布前内容不完整。 */
    COURSE_CONTENT_INCOMPLETE(4007, "课程内容不完整，请补齐基础信息、章节和小节"),

    /** 课程目录参数错误。 */
    CATALOG_PARAM_INVALID(4101, "课程目录参数不正确"),

    /** 课程目录中的课程不存在或不可见。 */
    CATALOG_COURSE_NOT_FOUND(4102, "课程不存在或未发布"),

    /** 当前用户无目录维护权限。 */
    CATALOG_FORBIDDEN(4103, "无权限维护课程目录"),

    /** 当前课程状态不允许维护目录。 */
    CATALOG_STATUS_INVALID(4104, "当前课程状态不允许维护目录"),

    /** 章节不存在。 */
    CATALOG_CHAPTER_NOT_FOUND(4105, "章节不存在"),

    /** 小节不存在。 */
    CATALOG_SECTION_NOT_FOUND(4106, "小节不存在"),

    /** 章节下存在小节。 */
    CATALOG_CHAPTER_HAS_SECTION(4107, "章节下存在小节，不能删除"),

    /** 小节下存在知识点。 */
    CATALOG_SECTION_HAS_CONCEPT(4108, "小节下存在知识点，不能删除"),

    /** 目录排序重复。 */
    CATALOG_SORT_DUPLICATED(4109, "目录排序重复");

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
     * 按接口合同返回对应 HTTP 状态码。
     *
     * @return HTTP 状态码
     */
    @Override
    public int httpStatus() {
        return switch (this) {
            case COURSE_NOT_FOUND,
                    CATALOG_COURSE_NOT_FOUND,
                    CATALOG_CHAPTER_NOT_FOUND,
                    CATALOG_SECTION_NOT_FOUND -> 404;
            case COURSE_FORBIDDEN,
                    CATALOG_FORBIDDEN -> 403;
            case COURSE_STATUS_INVALID,
                    COURSE_CONTENT_INCOMPLETE,
                    CATALOG_STATUS_INVALID,
                    CATALOG_CHAPTER_HAS_SECTION,
                    CATALOG_SECTION_HAS_CONCEPT,
                    CATALOG_SORT_DUPLICATED -> 409;
            default -> 400;
        };
    }
}
