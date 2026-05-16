package com.elysia.mooc.ai.tool.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.ai.tool.domain.enums.ToolCallStatus;
import com.elysia.mooc.ai.tool.domain.vo.ToolCallLogVO;
import com.elysia.mooc.ai.tool.service.ToolCallLogService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Tool 日志控制层合同测试。 */
@ExtendWith(MockitoExtension.class)
class ToolCallLogControllerWebTest {

    @Mock
    private ToolCallLogService toolCallLogService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ToolCallLogController(toolCallLogService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listToolLogsShouldReturnPageResult() throws Exception {
        ToolCallLogVO log = new ToolCallLogVO();
        log.setId(18001L);
        log.setConversationId(15001L);
        log.setMessageId(15101L);
        log.setUserId(4L);
        log.setToolName("CourseSearchTool");
        Map<String, Object> arguments = Map.of("keyword", "Java");
        Map<String, Object> result = Map.of("resultSummary", "找到 1 门课程");
        log.setArguments(arguments);
        log.setArgumentsJson(arguments);
        log.setResult(result);
        log.setResultJson(result);
        log.setStatus(ToolCallStatus.SUCCESS);
        log.setCostMs(42);
        log.setCreateTime(LocalDateTime.of(2026, 5, 16, 17, 0));
        when(toolCallLogService.listLogs(any())).thenReturn(PageResult.of(1L, 10, List.of(log)));

        mockMvc.perform(get("/api/admin/ai/tool-logs")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("toolName", "CourseSearchTool"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].toolName").value("CourseSearchTool"))
                .andExpect(jsonPath("$.data.list[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.list[0].arguments.keyword").value("Java"))
                .andExpect(jsonPath("$.data.list[0].argumentsJson.keyword").value("Java"))
                .andExpect(jsonPath("$.data.list[0].resultJson.resultSummary").value("找到 1 门课程"));
    }
}
