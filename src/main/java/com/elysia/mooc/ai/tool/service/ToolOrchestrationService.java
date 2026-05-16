package com.elysia.mooc.ai.tool.service;

import com.elysia.mooc.ai.tool.domain.vo.ToolCallResult;
import com.elysia.mooc.auth.security.LoginUser;
import java.util.List;

/** 普通聊天工具编排服务。 */
public interface ToolOrchestrationService {

    /**
     * 根据用户输入触发允许的只读工具。
     *
     * @param message        用户输入
     * @param conversationId 会话 ID
     * @param messageId      用户消息 ID
     * @param loginUser      当前登录用户
     * @return 本轮工具调用结果
     */
    List<ToolCallResult> planAndExecute(String message, Long conversationId, Long messageId, LoginUser loginUser);

    /**
     * 生成写入模型上下文的工具摘要。
     *
     * @param results 工具调用结果
     * @return 中文上下文
     */
    String buildToolContext(List<ToolCallResult> results);
}
