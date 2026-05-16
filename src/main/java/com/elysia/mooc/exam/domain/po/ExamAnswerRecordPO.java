package com.elysia.mooc.exam.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Data;

/** 答案记录实体，映射 exam_answer_record 表。 */
@Data
@TableName("exam_answer_record")
public class ExamAnswerRecordPO {

    /** 答案记录 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作答记录 ID。 */
    private Long recordId;

    /** 题目 ID。 */
    private Long questionId;

    /** 用户答案内容。 */
    private String answerContent;

    /** 是否正确：1 正确，0 错误，null 表示待人工批改。 */
    private Integer correct;

    /** 本题得分。 */
    private BigDecimal score;

    /** 教师评语或系统说明。 */
    private String teacherComment;
}
