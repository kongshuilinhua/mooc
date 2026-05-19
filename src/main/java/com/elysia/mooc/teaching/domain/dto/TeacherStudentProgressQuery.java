package com.elysia.mooc.teaching.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.teaching.domain.enums.TeacherStudentRiskLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 教师端学员进度分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherStudentProgressQuery extends PageQuery {

    /** 风险等级筛选：NORMAL、ATTENTION、RISK。 */
    private TeacherStudentRiskLevel riskLevel;

    /**
     * 兼容旧前端 page 字段，统一映射到 pageNo。
     * @param page 旧分页页码
     */
    public void setPage(Integer page) {
        setPageNo(page);
    }
}
