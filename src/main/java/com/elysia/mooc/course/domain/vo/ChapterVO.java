package com.elysia.mooc.course.domain.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 课程章节展示对象。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterVO {

    /** 章节 ID。 */
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 章节标题。 */
    private String title;

    /** 章节简介。 */
    private String summary;

    /** 展示排序。 */
    private Integer sort;

    /** 章节小节。 */
    private List<SectionVO> sections;
}
