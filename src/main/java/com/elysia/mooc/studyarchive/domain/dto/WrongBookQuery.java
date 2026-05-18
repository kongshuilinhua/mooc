package com.elysia.mooc.studyarchive.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.studyarchive.domain.enums.WrongBookMasteryLevel;
import com.elysia.mooc.studyarchive.domain.enums.WrongBookSourceType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 错题本分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WrongBookQuery extends PageQuery {

    /** 错题来源类型。 */
    private WrongBookSourceType sourceType;

    /** 掌握程度。 */
    private WrongBookMasteryLevel masteryLevel;

    /**
     * 兼容前端旧字段 page。
     *
     * @param page 前端旧页码字段
     */
    public void setPage(Integer page) {
        setPageNo(page);
    }
}
