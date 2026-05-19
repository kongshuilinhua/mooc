package com.elysia.mooc.ai.generator.constants;

/** AI 生成模块常量。 */
public final class AiGeneratorConstants {

    /** 默认章节总结风格。 */
    public static final String DEFAULT_SUMMARY_STYLE = "教学重点";

    /** 默认章节总结长度。 */
    public static final String DEFAULT_SUMMARY_LENGTH = "MEDIUM";

    /** 默认出题数量。 */
    public static final int DEFAULT_QUESTION_COUNT = 3;

    /** 单次最多生成题目数量。 */
    public static final int MAX_QUESTION_COUNT = 10;

    /** 学习路径默认周期天数。 */
    public static final int DEFAULT_HORIZON_DAYS = 30;

    /** 学习路径最大周期天数。 */
    public static final int MAX_HORIZON_DAYS = 180;

    /** 学习路径默认每日学习时长。 */
    public static final int DEFAULT_DAILY_MINUTES = 45;

    /** 章节总结模型来源。 */
    public static final String SOURCE_TYPE_CHAPTER = "CHAPTER";

    /** 小节素材来源。 */
    public static final String SOURCE_TYPE_SECTION = "SECTION";

    /** 知识点素材来源。 */
    public static final String SOURCE_TYPE_CONCEPT = "CONCEPT";

    /** 规则兜底来源标记。 */
    public static final String FALLBACK_SOURCE = "RULE_FALLBACK";

    private AiGeneratorConstants() {
    }
}
