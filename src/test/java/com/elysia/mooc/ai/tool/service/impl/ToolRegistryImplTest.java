package com.elysia.mooc.ai.tool.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.elysia.mooc.ai.tool.domain.dto.LearningProgressArguments;
import com.elysia.mooc.ai.tool.domain.dto.ToolCallRequest;
import com.elysia.mooc.ai.tool.domain.enums.ToolCallStatus;
import com.elysia.mooc.ai.tool.domain.vo.ToolCallResult;
import com.elysia.mooc.ai.tool.service.AiTool;
import com.elysia.mooc.ai.tool.service.ToolCallLogService;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.common.exception.BizException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Tool 注册表安全和日志测试。 */
class ToolRegistryImplTest {

    @Mock
    private ToolCallLogService toolCallLogService;

    private ToolRegistryImpl registry;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        registry = new ToolRegistryImpl(
                List.of(new FakeLearningProgressTool()),
                toolCallLogService,
                new ObjectMapper(),
                validator);
        when(toolCallLogService.saveLog(any(), any(), any(), any(), any(), any(), any(), anyLong(), any()))
                .thenReturn(18001L);
    }

    @Test
    void dispatchShouldLogSuccessWhenToolExists() {
        ToolCallResult result = registry.dispatch(ToolCallRequest.builder()
                .toolName("LearningProgressTool")
                .arguments(Map.of("courseId", 3001L))
                .conversationId(15001L)
                .messageId(15101L)
                .loginUser(new LoginUser(4L, "student", List.of("STUDENT"), List.of("ai:chat")))
                .build());

        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getLogId()).isEqualTo(18001L);
        assertThat(result.getResultSummary()).contains("进度");

        ArgumentCaptor<ToolCallStatus> statusCaptor = ArgumentCaptor.forClass(ToolCallStatus.class);
        org.mockito.Mockito.verify(toolCallLogService).saveLog(
                any(), any(), any(), any(), any(), any(), statusCaptor.capture(), anyLong(), any());
        assertThat(statusCaptor.getValue()).isEqualTo(ToolCallStatus.SUCCESS);
    }

    @Test
    void dispatchShouldLogFailedWhenToolMissing() {
        ToolCallResult result = registry.dispatch(ToolCallRequest.builder()
                .toolName("UnknownTool")
                .arguments(Map.of())
                .conversationId(15001L)
                .messageId(15101L)
                .loginUser(new LoginUser(4L, "student"))
                .build());

        assertThat(result.getSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("工具不存在");
        ArgumentCaptor<ToolCallStatus> statusCaptor = ArgumentCaptor.forClass(ToolCallStatus.class);
        org.mockito.Mockito.verify(toolCallLogService).saveLog(
                any(), any(), any(), any(), any(), any(), statusCaptor.capture(), anyLong(), any());
        assertThat(statusCaptor.getValue()).isEqualTo(ToolCallStatus.FAILED);
    }

    @Test
    void dispatchShouldRejectForgedUserId() {
        ToolCallResult result = registry.dispatch(ToolCallRequest.builder()
                .toolName("LearningProgressTool")
                .arguments(Map.of("courseId", 3001L, "userId", 5L))
                .loginUser(new LoginUser(4L, "student"))
                .build());

        assertThat(result.getSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("不允许查询他人的学习进度");
    }

    private static class FakeLearningProgressTool implements AiTool<LearningProgressArguments> {

        @Override
        public String name() {
            return "LearningProgressTool";
        }

        @Override
        public Class<LearningProgressArguments> argumentType() {
            return LearningProgressArguments.class;
        }

        @Override
        public Map<String, Object> execute(LearningProgressArguments arguments, LoginUser loginUser) {
            if (arguments.getUserId() != null && !arguments.getUserId().equals(loginUser.getUserId())) {
                throw new BizException(com.elysia.mooc.ai.tool.constants.ToolCallErrorCode.TOOL_FORBIDDEN,
                        "不允许查询他人的学习进度");
            }
            return Map.of("courseId", arguments.getCourseId(), "progressPercent", 56);
        }

        @Override
        public String summarize(Map<String, Object> result) {
            return "学习进度为 " + result.get("progressPercent") + "%。";
        }
    }
}
