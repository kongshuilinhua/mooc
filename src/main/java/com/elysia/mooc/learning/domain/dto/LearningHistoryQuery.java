package com.elysia.mooc.learning.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 学习历史分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LearningHistoryQuery extends PageQuery {

    /** 课程 ID。 */
    @Positive(message = "课程ID必须为正数")
    private Long courseId;

    /** 小节 ID。 */
    @Positive(message = "小节ID必须为正数")
    private Long sectionId;

    /** 课程或小节标题搜索关键字。 */
    @Size(max = 50, message = "搜索关键字不能超过50个字符")
    private String keyword;

    /** 排序字段白名单：createTime、lastHeartbeatTime、learnedSeconds。 */
    private String sortBy;

    /** 是否升序，默认倒序。 */
    private Boolean isAsc = Boolean.FALSE;
}
