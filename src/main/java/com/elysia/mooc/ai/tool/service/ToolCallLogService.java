package com.elysia.mooc.ai.tool.service;

import com.elysia.mooc.ai.tool.domain.dto.ToolCallLogQuery;
import com.elysia.mooc.ai.tool.domain.enums.ToolCallStatus;
import com.elysia.mooc.ai.tool.domain.vo.ToolCallLogVO;
import com.elysia.mooc.common.api.PageResult;
import java.util.Map;

/** Tool 调用日志服务。 */
public interface ToolCallLogService {

    /**
     * 保存工具调用日志。
     *
     * @param conversationId 会话 ID
     * @param messageId      消息 ID
     * @param userId         用户 ID
     * @param toolName       工具名
     * @param arguments      工具入参
     * @param result         工具结果
     * @param status         调用状态
     * @param costMs         耗时毫秒
     * @param errorMessage   中文错误
     * @return 日志 ID
     */
    Long saveLog(
            Long conversationId,
            Long messageId,
            Long userId,
            String toolName,
            Map<String, Object> arguments,
            Map<String, Object> result,
            ToolCallStatus status,
            long costMs,
            String errorMessage);

    /**
     * 分页查询工具日志。
     *
     * @param query 查询条件
     * @return 日志分页
     */
    PageResult<ToolCallLogVO> listLogs(ToolCallLogQuery query);
}
