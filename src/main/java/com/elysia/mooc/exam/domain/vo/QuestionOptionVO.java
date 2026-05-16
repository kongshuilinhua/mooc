package com.elysia.mooc.exam.domain.vo;

import lombok.Builder;
import lombok.Data;

/** 题目选项响应。 */
@Data
@Builder
public class QuestionOptionVO {

    /** 选项 ID。 */
    private Long id;

    /** 选项编码。 */
    private String optionKey;

    /** 选项内容。 */
    private String optionText;

    /** 是否正确，仅教师和管理员场景有意义。 */
    private Boolean correct;

    /** 排序值。 */
    private Integer sort;
}
