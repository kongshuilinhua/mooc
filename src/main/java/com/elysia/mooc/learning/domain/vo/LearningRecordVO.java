package com.elysia.mooc.learning.domain.vo;

import lombok.Builder;
import lombok.Data;

/** 小节学习记录视图。 */
@Data
@Builder
public class LearningRecordVO {

    /** 课程 ID。 */
    private Long courseId;

    /** 小节 ID。 */
    private Long sectionId;

    /** 兼容前端旧 videoId 口径。 */
    private Long videoId;

    /** 最近播放位置，单位秒。 */
    private Integer lastPlayTime;

    /** 历史最大播放位置，单位秒。 */
    private Integer maxHistoryTime;

    /** 是否完成。 */
    private Boolean completed;

    /** 当前播放位置，单位秒。 */
    private Integer position;

    /** 小节总时长，单位秒。 */
    private Integer duration;
}
