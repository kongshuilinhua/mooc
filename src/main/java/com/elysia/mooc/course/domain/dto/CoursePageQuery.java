package com.elysia.mooc.course.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.course.domain.enums.CourseDifficulty;
import com.elysia.mooc.course.domain.enums.CourseListScope;
import com.elysia.mooc.course.domain.enums.CoursePriceType;
import com.elysia.mooc.course.domain.enums.CourseSort;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 课程分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CoursePageQuery extends PageQuery {

    /** 标题搜索关键词。 */
    @Size(max = 50, message = "搜索关键字不能超过50个字符")
    private String keyword;

    /** 分类 ID。 */
    private Long categoryId;

    /** 标签 ID。 */
    private Long tagId;

    /** 课程难度。 */
    private CourseDifficulty difficulty;

    /** 价格类型。 */
    private CoursePriceType priceType;

    /** 课程状态，仅 MINE/ALL 范围生效。 */
    private CourseStatus status;

    /** 查询范围，默认公开课程。 */
    private CourseListScope scope = CourseListScope.PUBLIC;

    /** 排序方式，默认最新。 */
    private CourseSort sort = CourseSort.NEWEST;
}
