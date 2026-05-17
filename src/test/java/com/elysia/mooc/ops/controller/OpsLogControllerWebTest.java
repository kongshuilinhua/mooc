package com.elysia.mooc.ops.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.StringToBaseEnumConverterFactory;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.elysia.mooc.common.idempotent.domain.enums.IdempotentStatus;
import com.elysia.mooc.ops.domain.dto.AuditLogQuery;
import com.elysia.mooc.ops.domain.dto.IdempotentRecordQuery;
import com.elysia.mooc.ops.domain.vo.AuditLogVO;
import com.elysia.mooc.ops.domain.vo.IdempotentRecordVO;
import com.elysia.mooc.ops.service.OpsLogService;
import java.time.LocalDateTime;
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

/** 管理端审计与幂等查询接口合同测试。 */
@ExtendWith(MockitoExtension.class)
class OpsLogControllerWebTest {

    @Mock
    private OpsLogService opsLogService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverterFactory(new StringToBaseEnumConverterFactory());
        mockMvc = MockMvcBuilders.standaloneSetup(new OpsLogController(opsLogService))
                .setConversionService(conversionService)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listAuditLogsShouldReturnPageResult() throws Exception {
        AuditLogVO item = new AuditLogVO();
        item.setId(1L);
        item.setOperatorId(1L);
        item.setOperatorName("admin");
        item.setAction("COURSE_APPROVE");
        item.setTargetType("COURSE");
        item.setTargetId("3001");
        item.setRequestMethod("POST");
        item.setRequestPath("/api/admin/courses/3001/approve");
        item.setTraceId("trace-1");
        item.setSuccess(true);
        item.setCostMs(12);
        item.setCreateTime(LocalDateTime.now());
        when(opsLogService.listAuditLogs(any())).thenReturn(PageResult.of(1L, 10, List.of(item)));

        mockMvc.perform(get("/api/admin/audit-logs?pageNo=1&pageSize=10&success=true&action=COURSE_APPROVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].action").value("COURSE_APPROVE"))
                .andExpect(jsonPath("$.data.list[0].success").value(true));

        ArgumentCaptor<AuditLogQuery> captor = ArgumentCaptor.forClass(AuditLogQuery.class);
        verify(opsLogService).listAuditLogs(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getSuccess()).isTrue();
    }

    @Test
    void listIdempotentRecordsShouldAcceptStatusEnum() throws Exception {
        IdempotentRecordVO item = new IdempotentRecordVO();
        item.setId(1L);
        item.setIdempotentKey("ORDER_CREATE:3:abc");
        item.setBizType("ORDER_CREATE");
        item.setStatus(IdempotentStatus.SUCCESS);
        item.setStatusText("成功");
        item.setExpireTime(LocalDateTime.now().plusDays(1));
        item.setCreateTime(LocalDateTime.now());
        when(opsLogService.listIdempotentRecords(any())).thenReturn(PageResult.of(1L, 10, List.of(item)));

        mockMvc.perform(get("/api/admin/idempotent-records?pageNo=1&pageSize=10&status=SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].status").value(1))
                .andExpect(jsonPath("$.data.list[0].statusText").value("成功"));

        ArgumentCaptor<IdempotentRecordQuery> captor = ArgumentCaptor.forClass(IdempotentRecordQuery.class);
        verify(opsLogService).listIdempotentRecords(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getStatus()).isEqualTo(IdempotentStatus.SUCCESS);
    }
}
