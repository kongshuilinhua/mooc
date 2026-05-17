package com.elysia.mooc.recommend.domain.vo;

import com.elysia.mooc.course.domain.enums.CourseDifficulty;
import com.elysia.mooc.course.domain.enums.CoursePriceType;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** 推荐课程视图对象。 */
@Data
public class RecommendedCourseVO {

    /** 课程 ID。 */
    private Long id;

    /** 课程标题。 */
    private String title;

    /** 课程简介。 */
    private String summary;

    /** 课程封面。 */
    private String coverUrl;

    /** 分类 ID。 */
    private Long categoryId;

    /** 分类名称。 */
    private String categoryName;

    /** 标签 ID 列表。 */
    private List<Long> tagIds;

    /** 标签名称列表。 */
    private List<String> tagNames;

    /** 讲师 ID。 */
    private Long teacherId;

    /** 讲师名称。 */
    private String teacherName;

    /** 课程难度。 */
    private CourseDifficulty difficulty;

    /** 课程价格。 */
    private BigDecimal price;

    /** 价格类型。 */
    private CoursePriceType priceType;

    /** 课程状态。 */
    private CourseStatus status;

    /** 学习人数。 */
    private Integer learnCount;

    /** 收藏人数。 */
    private Integer favoriteCount;

    /** 平均评分。 */
    private BigDecimal ratingScore;

    /** 热度分。 */
    private BigDecimal hotScore;

    /** 推荐原因。 */
    private String reason;

    /** 发布时间。 */
    private LocalDateTime publishTime;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
