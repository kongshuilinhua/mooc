package com.elysia.mooc.course.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 课程知识点展示对象。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConceptVO {

    /** 知识点 ID。 */
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 关联小节 ID。 */
    private Long sectionId;

    /** 知识点标题。 */
    private String title;

    /** 知识点说明。 */
    private String content;

    /** 视频内开始秒数。 */
    private Integer startSecond;

    /** 视频内结束秒数。 */
    private Integer endSecond;

    /** 展示排序。 */
    private Integer sort;
}
