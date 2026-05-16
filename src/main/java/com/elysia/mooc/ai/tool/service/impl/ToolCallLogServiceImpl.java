package com.elysia.mooc.ai.tool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.ai.tool.domain.dto.ToolCallLogQuery;
import com.elysia.mooc.ai.tool.domain.enums.ToolCallStatus;
import com.elysia.mooc.ai.tool.domain.po.AiToolCallLogPO;
import com.elysia.mooc.ai.tool.domain.vo.ToolCallLogVO;
import com.elysia.mooc.ai.tool.mapper.AiToolCallLogMapper;
import com.elysia.mooc.ai.tool.service.ToolCallLogService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** Tool 调用日志服务实现。 */
@Service
@RequiredArgsConstructor
public class ToolCallLogServiceImpl implements ToolCallLogService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final int ERROR_MAX_LENGTH = 1000;

    private final AiToolCallLogMapper toolCallLogMapper;
    private final ObjectMapper objectMapper;

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
    @Override
    public Long saveLog(
            Long conversationId,
            Long messageId,
            Long userId,
            String toolName,
            Map<String, Object> arguments,
            Map<String, Object> result,
            ToolCallStatus status,
            long costMs,
            String errorMessage) {
        AiToolCallLogPO log = new AiToolCallLogPO();
        log.setConversationId(conversationId);
        log.setMessageId(messageId);
        log.setUserId(userId);
        log.setToolName(toolName);
        log.setArgumentsJson(toJson(desensitize(arguments)));
        log.setResultJson(toJson(desensitize(result)));
        log.setStatus(status);
        log.setCostMs(costMs > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) costMs);
        log.setErrorMessage(abbreviate(errorMessage, ERROR_MAX_LENGTH));
        toolCallLogMapper.insert(log);
        return log.getId();
    }

    /**
     * 分页查询工具日志。
     *
     * @param query 查询条件
     * @return 日志分页
     */
    @Override
    public PageResult<ToolCallLogVO> listLogs(ToolCallLogQuery query) {
        ToolCallLogQuery safeQuery = query == null ? new ToolCallLogQuery() : query;
        LambdaQueryWrapper<AiToolCallLogPO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(safeQuery.getToolName())) {
            wrapper.eq(AiToolCallLogPO::getToolName, safeQuery.getToolName().trim());
        }
        if (safeQuery.getStatus() != null) {
            wrapper.eq(AiToolCallLogPO::getStatus, safeQuery.getStatus());
        }
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            String keyword = safeQuery.getKeyword().trim();
            wrapper.and(nested -> nested.like(AiToolCallLogPO::getToolName, keyword)
                    .or()
                    .like(AiToolCallLogPO::getErrorMessage, keyword));
        }
        applyOrder(wrapper, safeQuery);
        Page<AiToolCallLogPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        return PageResult.of(toolCallLogMapper.selectPage(page, wrapper), this::toVO);
    }

    private void applyOrder(LambdaQueryWrapper<AiToolCallLogPO> wrapper, ToolCallLogQuery query) {
        boolean asc = Boolean.TRUE.equals(query.getIsAsc());
        String sortBy = query.getSortBy();
        if ("costMs".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, AiToolCallLogPO::getCostMs);
        } else if ("id".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, AiToolCallLogPO::getId);
        } else {
            wrapper.orderBy(true, asc, AiToolCallLogPO::getCreateTime);
        }
        wrapper.orderByDesc(AiToolCallLogPO::getId);
    }

    private ToolCallLogVO toVO(AiToolCallLogPO po) {
        return BeanCopyUtils.copyBean(po, ToolCallLogVO.class, (source, target) -> {
            Map<String, Object> arguments = parseJson(source.getArgumentsJson());
            Map<String, Object> result = parseJson(source.getResultJson());
            target.setArguments(arguments);
            target.setArgumentsJson(arguments);
            target.setResult(result);
            target.setResultJson(result);
        });
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Collections.emptyMap() : value);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private Map<String, Object> parseJson(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyMap();
        }
        try {
            Map<String, Object> value = objectMapper.readValue(json, MAP_TYPE);
            return value == null ? Collections.emptyMap() : value;
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private Map<String, Object> desensitize(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyMap();
        }
        java.util.LinkedHashMap<String, Object> result = new java.util.LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (isSensitiveKey(key)) {
                result.put(key, "***");
            } else {
                result.put(key, value);
            }
        });
        return result;
    }

    private boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String lower = key.toLowerCase();
        return lower.contains("token")
                || lower.contains("password")
                || lower.contains("secret")
                || lower.contains("authorization");
    }

    private String abbreviate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength - 1)) + "…";
    }
}
