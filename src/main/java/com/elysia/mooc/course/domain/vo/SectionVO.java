package com.elysia.mooc.course.domain.vo;

import com.elysia.mooc.common.enums.EnableStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 课程小节展示对象。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionVO {

    /** 小节 ID。 */
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 章节 ID。 */
    private Long chapterId;

    /** 小节标题。 */
    private String title;

    /** 媒资 ID。 */
    private Long mediaId;

    /** 视频时长，单位秒。 */
    private Integer durationSeconds;

    /** 是否可试看。 */
    private Boolean freePreview;

    /** 展示排序。 */
    private Integer sort;

    /** 小节启停状态，接口返回 1/0。 */
    private EnableStatus status;

    /** 小节知识点。 */
    private List<ConceptVO> concepts;
}
