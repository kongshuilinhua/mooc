package com.elysia.mooc.learning.constants;

/** 学习模块常量。 */
public final class LearningConstants {

    /** 单次心跳最多累计 60 秒，避免客户端异常或重复请求刷学习时长。 */
    public static final int MAX_HEARTBEAT_SECONDS = 60;

    /** 播放到 90% 视为小节完成。 */
    public static final double SECTION_FINISH_THRESHOLD = 0.9D;

    private LearningConstants() {
    }
}
