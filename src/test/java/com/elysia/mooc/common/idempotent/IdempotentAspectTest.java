package com.elysia.mooc.common.idempotent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.audit.SensitiveFieldMasker;
import com.elysia.mooc.common.idempotent.domain.enums.IdempotentStatus;
import com.elysia.mooc.common.idempotent.domain.po.IdempotentRecordPO;
import com.elysia.mooc.common.idempotent.service.IdempotentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** 幂等切面测试。 */
class IdempotentAspectTest {

    private final IdempotentService idempotentService = Mockito.mock(IdempotentService.class);
    private final IdempotentAspect aspect = new IdempotentAspect(
            idempotentService,
            new ObjectMapper(),
            new SensitiveFieldMasker());

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void aroundShouldReturnHistoricalApiResultWhenSuccessRecordExists() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/orders");
        request.addHeader("X-Idempotency-Key", "abc");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Idempotent annotation = Mockito.mock(Idempotent.class);
        when(annotation.bizType()).thenReturn("ORDER_CREATE");
        when(annotation.bizId()).thenReturn("");
        when(annotation.expireSeconds()).thenReturn(86_400L);

        ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(signature.getMethod()).thenReturn(DummyController.class.getMethod("create"));

        IdempotentRecordPO record = new IdempotentRecordPO();
        record.setId(1L);
        record.setNewlyCreated(false);
        record.setStatus(IdempotentStatus.SUCCESS);
        record.setExpireTime(LocalDateTime.now().plusDays(1));
        record.setResponseBody("{\"code\":200,\"message\":\"操作成功\",\"data\":true,\"traceId\":\"t\"}");
        when(idempotentService.buildRecordKey(eq("ORDER_CREATE"), any(), eq("abc"))).thenReturn("ORDER_CREATE:anonymous:abc");
        when(idempotentService.tryCreateProcessing(any(), eq("ORDER_CREATE"), any(), any(), any())).thenReturn(record);

        Object result = aspect.around(joinPoint, annotation);

        assertThat(result).isInstanceOf(ApiResult.class);
        assertThat(((ApiResult<?>) result).getData()).isEqualTo(true);
        verify(joinPoint, never()).proceed();
    }

    static class DummyController {
        public ApiResult<Boolean> create() {
            return ApiResult.ok(Boolean.TRUE);
        }
    }
}
