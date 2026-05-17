package com.elysia.mooc.recommend.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

/** 热门知识点视图对象。 */
@Data
public class HotConceptVO {

    /** 知识点 ID。 */
    private Long conceptId;

    /** 知识点名称。 */
    private String conceptName;

    /** 课程 ID。 */
    private Long courseId;

    /** 课程标题。 */
    private String courseTitle;

    /** 热度分。 */
    private BigDecimal score;

    /** 命中次数。 */
    private Integer hitCount;
}
