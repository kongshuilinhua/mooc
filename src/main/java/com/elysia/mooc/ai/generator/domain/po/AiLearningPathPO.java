package com.elysia.mooc.ai.generator.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.ai.generator.domain.enums.AiLearningPathStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** AI 学习路径实体，映射 ai_learning_path 表。 */
@Data
@TableName("ai_learning_path")
public class AiLearningPathPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学生用户 ID。 */
    private Long studentId;

    /** 目标类型。 */
    private String goalType;

    /** 目标课程 ID。 */
    private Long targetCourseId;

    /** 路径内容快照，SQL 字段为 LONGTEXT。 */
    private String pathContent;

    /** 路径状态。 */
    private AiLearningPathStatus status;

    /** 失效时间。 */
    private LocalDateTime expireTime;

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
