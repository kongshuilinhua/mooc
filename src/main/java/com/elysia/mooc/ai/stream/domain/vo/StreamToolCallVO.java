package com.elysia.mooc.ai.stream.domain.vo;

import java.util.Collections;
import java.util.Map;
import lombok.Data;

/** SSE Tool 调用事件。 */
@Data
public class StreamToolCallVO {

    /** 工具名称。 */
    private String toolName;

    /** 工具入参。 */
    private Map<String, Object> arguments = Collections.emptyMap();

    /** 是否成功。 */
    private Boolean success;

    /** 结果摘要。 */
    private String resultSummary;

    /** 调用耗时。 */
    private Long latencyMs;

    /** 中文错误信息。 */
    private String errorMessage;
}
