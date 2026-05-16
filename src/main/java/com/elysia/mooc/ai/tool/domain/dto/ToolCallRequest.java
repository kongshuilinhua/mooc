package com.elysia.mooc.ai.tool.domain.dto;

import com.elysia.mooc.auth.security.LoginUser;
import java.util.Collections;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** Tool 调用请求，服务端内部生成，不能直接信任模型传入的身份字段。 */
@Data
@Builder
public class ToolCallRequest {

    /** 工具名称，必须命中注册表。 */
    private String toolName;

    /** 工具原始入参。 */
    @Builder.Default
    private Map<String, Object> arguments = Collections.emptyMap();

    /** AI 会话 ID。 */
    private Long conversationId;

    /** 触发工具调用的消息 ID。 */
    private Long messageId;

    /** 当前登录用户。 */
    private LoginUser loginUser;
}
