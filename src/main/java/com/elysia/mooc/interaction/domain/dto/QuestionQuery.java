package com.elysia.mooc.interaction.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.interaction.domain.enums.QuestionStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 问题分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionQuery extends PageQuery {

    /** 搜索关键字。 */
    @Size(max = 50, message = "搜索关键字不能超过50个字符")
    private String keyword;

    /** 状态筛选，仅允许 OPEN 或 RESOLVED 对外展示。 */
    private QuestionStatus status;

    /** 排序字段白名单：createTime、answerCount。 */
    private String sortBy;

    /** 是否升序，默认倒序。 */
    private Boolean isAsc = Boolean.FALSE;
}
