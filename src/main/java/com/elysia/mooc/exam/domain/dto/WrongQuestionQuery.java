package com.elysia.mooc.exam.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 错题本分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WrongQuestionQuery extends PageQuery {

    /** 课程 ID。 */
    private Long courseId;

    /** 题干关键字。 */
    @Size(max = 50, message = "搜索关键字不能超过50个字符")
    private String keyword;

    /** 是否已解决。 */
    private Boolean resolved;
}
