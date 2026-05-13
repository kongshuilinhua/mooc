package com.elysia.mooc.course.domain.vo;

import com.elysia.mooc.course.domain.enums.CourseStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 课程目录树展示对象。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseCatalogVO {

    /** 课程 ID。 */
    private Long courseId;

    /** 课程标题。 */
    private String courseTitle;

    /** 课程状态。 */
    private CourseStatus courseStatus;

    /** 章节数量。 */
    private Integer chapterCount;

    /** 小节数量。 */
    private Integer sectionCount;

    /** 总时长，单位秒。 */
    private Integer durationSeconds;

    /** 章节树。 */
    private List<ChapterVO> chapters;

    /** 课程级知识点。 */
    private List<ConceptVO> concepts;
}
