package com.elysia.mooc.knowledge.parse;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** 文档切片配置。 */
@Data
@ConfigurationProperties(prefix = "mooc.ai.segment")
public class SegmentConfig {

    /** 单个切片最大字符数。 */
    private int maxLength = 800;

    /** 相邻切片重叠字符数。 */
    private int overlapLength = 100;

    /**
     * 获取安全最大长度。
     *
     * @return 大于 0 的最大长度
     */
    public int safeMaxLength() {
        return maxLength <= 0 ? 800 : maxLength;
    }

    /**
     * 获取安全重叠长度。
     *
     * @return 小于最大长度的重叠长度
     */
    public int safeOverlapLength() {
        int safeMaxLength = safeMaxLength();
        if (overlapLength < 0) {
            return 0;
        }
        return Math.min(overlapLength, safeMaxLength - 1);
    }
}
