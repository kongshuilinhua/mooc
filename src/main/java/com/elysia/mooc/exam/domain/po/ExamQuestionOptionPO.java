package com.elysia.mooc.exam.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 题目选项实体，映射 exam_question_option 表。 */
@Data
@TableName("exam_question_option")
public class ExamQuestionOptionPO {

    /** 选项 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题目 ID。 */
    private Long questionId;

    /** 选项编码，例如 A/B/C/D。 */
    private String optionKey;

    /** 选项内容。 */
    private String optionText;

    /** 是否正确：1 正确，0 错误。 */
    private Integer correct;

    /** 排序值。 */
    private Integer sort;
}
