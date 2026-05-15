package com.elysia.mooc.event.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.StringToBaseEnumConverterFactory;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.elysia.mooc.event.constants.EventErrorCode;
import com.elysia.mooc.event.domain.dto.EventPublishLogQuery;
import com.elysia.mooc.event.domain.enums.EventConsumeStatus;
import com.elysia.mooc.event.domain.enums.EventPublishStatus;
import com.elysia.mooc.event.domain.vo.EventConsumeLogVO;
import com.elysia.mooc.event.domain.vo.EventPublishLogVO;
import com.elysia.mooc.event.service.EventAdminService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** 管理端事件接口 HTTP 合同测试。 */
@ExtendWith(MockitoExtension.class)
class EventAdminControllerWebTest {

    @Mock
    private EventAdminService eventAdminService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverterFactory(new StringToBaseEnumConverterFactory());
        mockMvc = MockMvcBuilders.standaloneSetup(new EventAdminController(eventAdminService))
                .setConversionService(conversionService)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listPublishLogsShouldReturnPageResultAndBindStatus() throws Exception {
        EventPublishLogVO vo = new EventPublishLogVO();
        vo.setId(1L);
        vo.setEventId("evt-course-publish-3001");
        vo.setTopic("mooc.course.published");
        vo.setEventType("COURSE_PUBLISHED");
        vo.setStatus(EventPublishStatus.SENT);
        when(eventAdminService.listPublishLogs(any()))
                .thenReturn(PageResult.of(1L, 10, List.of(vo)));

        mockMvc.perform(get("/api/admin/events/publish-logs")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("status", "SENT")
                        .param("sortBy", "createTime")
                        .param("isAsc", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.totalPage").value(1))
                .andExpect(jsonPath("$.data.list[0].eventId").value("evt-course-publish-3001"))
                .andExpect(jsonPath("$.data.list[0].status").value("SENT"));

        ArgumentCaptor<EventPublishLogQuery> captor = ArgumentCaptor.forClass(EventPublishLogQuery.class);
        verify(eventAdminService).listPublishLogs(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getStatus()).isEqualTo(EventPublishStatus.SENT);
    }

    @Test
    void listConsumeLogsShouldReturnPageResult() throws Exception {
        EventConsumeLogVO vo = new EventConsumeLogVO();
        vo.setId(2L);
        vo.setEventId("evt-answer-1");
        vo.setTopic("mooc.interaction.answer.created");
        vo.setConsumerGroup("message-center");
        vo.setStatus(EventConsumeStatus.SUCCESS);
        when(eventAdminService.listConsumeLogs(any()))
                .thenReturn(PageResult.of(1L, 10, List.of(vo)));

        mockMvc.perform(get("/api/admin/events/consume-logs")
                        .param("status", "SUCCESS")
                        .param("consumerGroup", "message-center"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].consumerGroup").value("message-center"))
                .andExpect(jsonPath("$.data.list[0].status").value("SUCCESS"));
    }

    @Test
    void listPublishLogsShouldReturn400WhenStatusInvalid() throws Exception {
        mockMvc.perform(get("/api/admin/events/publish-logs")
                        .param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数类型错误或枚举值不合法"));
    }

    @Test
    void retryShouldTreatEventIdAsString() throws Exception {
        when(eventAdminService.retry("evt-order-paid-21001")).thenReturn(true);

        mockMvc.perform(post("/api/admin/events/evt-order-paid-21001/retry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void retryShouldReturn404WhenEventMissing() throws Exception {
        when(eventAdminService.retry("evt-missing"))
                .thenThrow(new BizException(EventErrorCode.EVENT_NOT_FOUND));

        mockMvc.perform(post("/api/admin/events/evt-missing/retry"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("事件不存在"));
    }
}
