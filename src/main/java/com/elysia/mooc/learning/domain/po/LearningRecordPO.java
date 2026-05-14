package com.elysia.mooc.learning.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.learning.domain.enums.LearningFinishedStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 小节学习记录实体，映射 learning_record 表。 */
@Data
@TableName("learning_record")
public class LearningRecordPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 课程 ID。 */
    private Long courseId;

    /** 小节 ID。 */
    private Long sectionId;

    /** 最近播放位置，单位秒。 */
    private Integer lastPosition;

    /** 累计学习秒数。 */
    private Integer learnedSeconds;

    /** 小节总时长，单位秒。 */
    private Integer durationSeconds;

    /** 是否完成。 */
    private LearningFinishedStatus finished;

    /** 最近心跳时间。 */
    private LocalDateTime lastHeartbeatTime;

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
