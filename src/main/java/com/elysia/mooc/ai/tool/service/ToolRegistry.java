package com.elysia.mooc.ai.tool.service;

import com.elysia.mooc.ai.tool.domain.dto.ToolCallRequest;
import com.elysia.mooc.ai.tool.domain.vo.ToolCallResult;

/** Tool 注册表，负责白名单查找、参数解析、执行和日志闭环。 */
public interface ToolRegistry {

    /**
     * 分派执行工具。
     *
     * @param request 工具调用请求
     * @return 工具调用结果
     */
    ToolCallResult dispatch(ToolCallRequest request);
}
