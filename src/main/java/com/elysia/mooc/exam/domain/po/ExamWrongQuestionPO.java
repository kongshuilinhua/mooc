package com.elysia.mooc.exam.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 错题本实体，映射 exam_wrong_question 表。 */
@Data
@TableName("exam_wrong_question")
public class ExamWrongQuestionPO {

    /** 错题记录 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 题目 ID。 */
    private Long questionId;

    /** 课程 ID。 */
    private Long courseId;

    /** 错误次数。 */
    private Integer wrongCount;

    /** 最近答错时间。 */
    private LocalDateTime lastWrongTime;

    /** 是否已解决：1 已解决，0 未解决。 */
    private Integer resolved;

    /** 创建时间。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人。 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 更新人。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 逻辑删除标记。 */
    @TableLogic
    private Integer deleted;
}
