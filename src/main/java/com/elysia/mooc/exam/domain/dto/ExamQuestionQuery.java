package com.elysia.mooc.exam.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.exam.domain.enums.ExamDifficulty;
import com.elysia.mooc.exam.domain.enums.ExamQuestionType;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 题目分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExamQuestionQuery extends PageQuery {

    /** 搜索关键字，匹配题干。 */
    @Size(max = 50, message = "搜索关键字不能超过50个字符")
    private String keyword;

    /** 课程 ID。 */
    private Long courseId;

    /** 题型。 */
    private ExamQuestionType questionType;

    /** 难度。 */
    private ExamDifficulty difficulty;

    /** 状态。 */
    private EnableStatus status;

    /** 排序字段白名单：createTime、score。 */
    private String sortBy;

    /** 是否升序。 */
    private Boolean isAsc = false;
}
