package com.elysia.mooc.learning.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.learning.domain.enums.LearningBehaviorType;
import java.time.LocalDateTime;
import lombok.Data;

/** 学习行为日志实体，映射 learning_behavior_log 表。 */
@Data
@TableName("learning_behavior_log")
public class LearningBehaviorLogPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 课程 ID。 */
    private Long courseId;

    /** 小节 ID。 */
    private Long sectionId;

    /** 行为类型。 */
    private LearningBehaviorType eventType;

    /** 播放位置秒数。 */
    private Integer positionSecond;

    /** JSON 扩展信息。 */
    private String extra;

    /** 创建时间。 */
    @TableField("create_time")
    private LocalDateTime createTime;
}
