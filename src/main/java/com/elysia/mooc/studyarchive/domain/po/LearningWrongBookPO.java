package com.elysia.mooc.studyarchive.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.studyarchive.domain.enums.WrongBookMasteryLevel;
import com.elysia.mooc.studyarchive.domain.enums.WrongBookSourceType;
import java.time.LocalDateTime;
import lombok.Data;

/** 错题本实体，映射 learning_wrong_book 表。 */
@Data
@TableName("learning_wrong_book")
public class LearningWrongBookPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学生用户 ID。 */
    private Long studentId;

    /** 题目 ID。 */
    private Long questionId;

    /** 来源类型。 */
    private WrongBookSourceType sourceType;

    /** 做错次数。 */
    private Integer wrongCount;

    /** 掌握程度。 */
    private WrongBookMasteryLevel masteryLevel;

    /** 最近做错时间。 */
    private LocalDateTime lastWrongTime;

    /** 创建时间。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人 ID。 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 更新人 ID。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 逻辑删除标记。 */
    @TableLogic
    private Integer deleted;
}
