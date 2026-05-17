package com.elysia.mooc.recommend.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 热门知识点查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HotConceptQuery extends PageQuery {

    /** 搜索关键字。 */
    @Size(max = 50, message = "搜索关键字不能超过50个字符")
    private String keyword;

    /** 课程 ID。 */
    private Long courseId;
}
