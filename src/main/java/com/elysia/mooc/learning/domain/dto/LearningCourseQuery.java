package com.elysia.mooc.learning.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.learning.domain.enums.LearningCourseStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 我的课程分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LearningCourseQuery extends PageQuery {

    /** 课程标题搜索关键字。 */
    @Size(max = 50, message = "搜索关键字不能超过50个字符")
    private String keyword;

    /** 学习状态筛选。 */
    private LearningCourseStatus status;

    /** 排序字段白名单：createTime、lastLearnTime、progressPercent。 */
    private String sortBy;

    /** 是否升序，默认倒序。 */
    private Boolean isAsc = Boolean.FALSE;
}
