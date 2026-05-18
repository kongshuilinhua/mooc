package com.elysia.mooc.studyarchive.domain.vo;

import com.elysia.mooc.studyarchive.domain.enums.LearningNoteStatus;
import com.elysia.mooc.studyarchive.domain.enums.LearningNoteType;
import java.time.LocalDateTime;
import lombok.Data;

/** 学习笔记响应。 */
@Data
public class LearningNoteVO {

    /** 笔记 ID。 */
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 小节 ID。 */
    private Long sectionId;

    /** 笔记内容。 */
    private String content;

    /** 笔记类型。 */
    private LearningNoteType noteType;

    /** 笔记类型说明。 */
    private String noteTypeDesc;

    /** 笔记状态。 */
    private LearningNoteStatus status;

    /** 笔记状态说明。 */
    private String statusDesc;

    /** 保存时间。 */
    private LocalDateTime createTime;
}
