package com.elysia.mooc.recommend.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 推荐和热门课程查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseRecommendQuery extends PageQuery {

    /** 搜索关键字。 */
    @Size(max = 50, message = "搜索关键字不能超过50个字符")
    private String keyword;

    /** 分类 ID。 */
    private Long categoryId;

    /** 标签 ID。 */
    private Long tagId;

    /** 排序字段白名单：hotScore、ratingScore、learnCount、favoriteCount、createTime。 */
    private String sortBy;

    /** 是否升序，默认 false。 */
    private Boolean isAsc = Boolean.FALSE;
}
