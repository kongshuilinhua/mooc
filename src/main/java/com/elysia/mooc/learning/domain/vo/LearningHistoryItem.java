package com.elysia.mooc.learning.domain.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/** 学习历史列表项。 */
@Data
@Builder
public class LearningHistoryItem {

    /** 学习记录 ID。 */
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 课程名称。 */
    private String courseName;

    /** 小节 ID，兼容前端旧 videoId 口径。 */
    private Long videoId;

    /** 小节标题，兼容前端旧 videoTitle 口径。 */
    private String videoTitle;

    /** 小节 ID。 */
    private Long sectionId;

    /** 小节标题。 */
    private String sectionName;

    /** 已学习秒数。 */
    private Integer learnedSeconds;

    /** 最近播放位置，单位秒。 */
    private Integer lastPosition;

    /** 小节总时长，单位秒。 */
    private Integer durationSeconds;

    /** 最近心跳时间。 */
    private LocalDateTime createTime;
}
