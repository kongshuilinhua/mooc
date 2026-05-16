package com.elysia.mooc.exam.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.exam.domain.enums.ExamPaperStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 试卷分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExamPaperQuery extends PageQuery {

    /** 搜索关键字，匹配试卷标题。 */
    @Size(max = 50, message = "搜索关键字不能超过50个字符")
    private String keyword;

    /** 课程 ID。 */
    private Long courseId;

    /** 试卷状态。 */
    private ExamPaperStatus status;

    /** 排序字段白名单：createTime、totalScore。 */
    private String sortBy;

    /** 是否升序。 */
    private Boolean isAsc = false;
}
