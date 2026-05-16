package com.elysia.mooc.ai.tool.domain.vo;

import com.elysia.mooc.ai.chat.domain.vo.AiToolCallVO;
import java.util.Collections;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** Tool 调用结果，供聊天响应、SSE 事件和日志服务复用。 */
@Data
@Builder(toBuilder = true)
public class ToolCallResult {

    /** 工具名称。 */
    private String toolName;

    /** 工具入参。 */
    @Builder.Default
    private Map<String, Object> arguments = Collections.emptyMap();

    /** 是否成功。 */
    private Boolean success;

    /** 工具原始结果摘要数据。 */
    @Builder.Default
    private Map<String, Object> result = Collections.emptyMap();

    /** 面向模型和前端的短摘要。 */
    private String resultSummary;

    /** 调用耗时。 */
    private Long latencyMs;

    /** 中文错误信息。 */
    private String errorMessage;

    /** 日志 ID。 */
    private Long logId;

    /**
     * 转换为聊天响应中的工具调用视图。
     *
     * @return 工具调用视图
     */
    public AiToolCallVO toAiToolCallVO() {
        AiToolCallVO vo = new AiToolCallVO();
        vo.setToolName(toolName);
        vo.setArguments(arguments == null ? Collections.emptyMap() : arguments);
        vo.setSuccess(Boolean.TRUE.equals(success));
        vo.setResultSummary(resultSummary);
        vo.setLatencyMs(latencyMs);
        vo.setErrorMessage(errorMessage);
        return vo;
    }
}
