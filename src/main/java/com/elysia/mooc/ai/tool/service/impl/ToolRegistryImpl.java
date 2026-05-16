package com.elysia.mooc.ai.tool.service.impl;

import com.elysia.mooc.ai.tool.constants.ToolCallConstants;
import com.elysia.mooc.ai.tool.constants.ToolCallErrorCode;
import com.elysia.mooc.ai.tool.domain.dto.ToolCallRequest;
import com.elysia.mooc.ai.tool.domain.enums.ToolCallStatus;
import com.elysia.mooc.ai.tool.domain.vo.ToolCallResult;
import com.elysia.mooc.ai.tool.service.AiTool;
import com.elysia.mooc.ai.tool.service.ToolCallLogService;
import com.elysia.mooc.ai.tool.service.ToolRegistry;
import com.elysia.mooc.common.exception.BizException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** Tool 注册表实现，统一收口白名单、参数校验和日志留痕。 */
@Component
@RequiredArgsConstructor
public class ToolRegistryImpl implements ToolRegistry {

    private final List<AiTool<?>> tools;
    private final ToolCallLogService toolCallLogService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    /**
     * 分派执行工具。
     *
     * @param request 工具调用请求
     * @return 工具调用结果
     */
    @Override
    public ToolCallResult dispatch(ToolCallRequest request) {
        long start = System.currentTimeMillis();
        String toolName = request == null ? null : request.getToolName();
        Map<String, Object> arguments = request == null || request.getArguments() == null
                ? Collections.emptyMap()
                : request.getArguments();
        try {
            AiTool<?> tool = toolMap().get(toolName);
            if (tool == null) {
                throw new BizException(ToolCallErrorCode.TOOL_NOT_FOUND);
            }
            ToolCallResult result = executeTool(tool, request, arguments, start);
            Long logId = saveLog(request, toolName, arguments, result, ToolCallStatus.SUCCESS);
            return result.toBuilder().logId(logId).build();
        } catch (BizException ex) {
            return failedResult(request, toolName, arguments, start, ex.getMessage());
        } catch (RuntimeException ex) {
            return failedResult(request, toolName, arguments, start, ToolCallErrorCode.TOOL_EXECUTE_FAILED.message());
        }
    }

    private Map<String, AiTool<?>> toolMap() {
        return tools.stream().collect(Collectors.toMap(AiTool::name, Function.identity(), (left, right) -> left));
    }

    private <T> ToolCallResult executeTool(
            AiTool<T> tool,
            ToolCallRequest request,
            Map<String, Object> arguments,
            long start) {
        T typedArguments = resolveArguments(tool.argumentType(), arguments);
        Map<String, Object> result = tool.execute(typedArguments, request.getLoginUser());
        long latencyMs = System.currentTimeMillis() - start;
        return ToolCallResult.builder()
                .toolName(tool.name())
                .arguments(arguments)
                .success(true)
                .result(result == null ? Collections.emptyMap() : result)
                .resultSummary(abbreviate(tool.summarize(result), ToolCallConstants.MAX_RESULT_SUMMARY_LENGTH))
                .latencyMs(latencyMs)
                .build();
    }

    private <T> T resolveArguments(Class<T> argumentType, Map<String, Object> arguments) {
        T typedArguments = objectMapper.convertValue(arguments == null ? Collections.emptyMap() : arguments, argumentType);
        Set<ConstraintViolation<T>> violations = validator.validate(typedArguments);
        if (!violations.isEmpty()) {
            String message = violations.iterator().next().getMessage();
            throw new BizException(ToolCallErrorCode.TOOL_PARAM_INVALID, message);
        }
        return typedArguments;
    }

    private ToolCallResult failedResult(
            ToolCallRequest request,
            String toolName,
            Map<String, Object> arguments,
            long start,
            String errorMessage) {
        long latencyMs = System.currentTimeMillis() - start;
        String message = StringUtils.hasText(errorMessage)
                ? errorMessage
                : ToolCallErrorCode.TOOL_EXECUTE_FAILED.message();
        ToolCallResult result = ToolCallResult.builder()
                .toolName(StringUtils.hasText(toolName) ? toolName : "UNKNOWN")
                .arguments(arguments)
                .success(false)
                .result(Collections.emptyMap())
                .resultSummary(message)
                .latencyMs(latencyMs)
                .errorMessage(message)
                .build();
        Long logId = saveLog(request, result.getToolName(), arguments, result, ToolCallStatus.FAILED);
        return result.toBuilder().logId(logId).build();
    }

    private Long saveLog(
            ToolCallRequest request,
            String toolName,
            Map<String, Object> arguments,
            ToolCallResult result,
            ToolCallStatus status) {
        Long conversationId = request == null ? null : request.getConversationId();
        Long messageId = request == null ? null : request.getMessageId();
        Long userId = request == null || request.getLoginUser() == null ? null : request.getLoginUser().getUserId();
        Map<String, Object> resultJson = new LinkedHashMap<>();
        resultJson.put("success", result.getSuccess());
        resultJson.put("resultSummary", result.getResultSummary());
        resultJson.put("data", result.getResult());
        return toolCallLogService.saveLog(
                conversationId,
                messageId,
                userId,
                toolName,
                arguments,
                resultJson,
                status,
                result.getLatencyMs() == null ? 0L : result.getLatencyMs(),
                result.getErrorMessage());
    }

    private String abbreviate(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength - 1)) + "…";
    }
}
