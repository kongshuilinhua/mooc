package com.elysia.mooc.recommend.constants;

/** 推荐与热门课程常量。 */
public final class RecommendConstants {

    /** 快照最多解析课程数量，避免坏数据拖慢公开接口。 */
    public static final int MAX_SNAPSHOT_COURSE_SIZE = 100;

    /** 快照推荐原因默认文案。 */
    public static final String DEFAULT_REASON = "热门课程推荐";

    /** 个性化推荐原因兜底文案。 */
    public static final String SNAPSHOT_REASON = "根据你的学习偏好推荐";

    /** 课程热度统计日期格式固定为当天。 */
    public static final int HOT_CONCEPT_DEFAULT_SCORE = 1;

    private RecommendConstants() {
    }
}
