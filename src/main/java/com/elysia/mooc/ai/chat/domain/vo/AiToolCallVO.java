package com.elysia.mooc.ai.chat.domain.vo;

import java.util.Map;
import lombok.Data;

/** AI 工具调用信息，day15 普通聊天默认返回空列表。 */
@Data
public class AiToolCallVO {

    /** 工具名称。 */
    private String toolName;

    /** 工具入参。 */
    private Map<String, Object> arguments;

    /** 是否调用成功。 */
    private Boolean success;

    /** 结果摘要。 */
    private String resultSummary;

    /** 调用耗时。 */
    private Long latencyMs;
}
