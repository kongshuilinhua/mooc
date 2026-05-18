package com.elysia.mooc.studyarchive.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.studyarchive.domain.enums.LearningNoteStatus;
import com.elysia.mooc.studyarchive.domain.enums.LearningNoteType;
import java.time.LocalDateTime;
import lombok.Data;

/** 学习笔记实体，映射 learning_note 表。 */
@Data
@TableName("learning_note")
public class LearningNotePO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学生用户 ID。 */
    private Long studentId;

    /** 课程 ID。 */
    private Long courseId;

    /** 小节 ID。 */
    private Long sectionId;

    /** 笔记内容。 */
    private String content;

    /** 笔记类型。 */
    private LearningNoteType noteType;

    /** 笔记状态。 */
    private LearningNoteStatus status;

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
