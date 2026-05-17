package com.elysia.mooc.common.idempotent;

import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.common.audit.SensitiveFieldMasker;
import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.idempotent.domain.enums.IdempotentStatus;
import com.elysia.mooc.common.idempotent.domain.po.IdempotentRecordPO;
import com.elysia.mooc.common.idempotent.service.IdempotentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** 写接口幂等切面。 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 90)
@RequiredArgsConstructor
public class IdempotentAspect {

    private static final String IDEMPOTENCY_HEADER = "X-Idempotency-Key";

    private final IdempotentService idempotentService;
    private final ObjectMapper objectMapper;
    private final SensitiveFieldMasker sensitiveFieldMasker;
    private final SpelExpressionParser expressionParser = new SpelExpressionParser();

    /**
     * 围绕幂等写接口执行去重控制。
     *
     * @param joinPoint 切点
     * @param idempotent 幂等注解
     * @return 原接口返回或历史成功响应
     * @throws Throwable 原方法异常
     */
    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        HttpServletRequest request = currentRequest();
        String rawKey = request == null ? null : request.getHeader(IDEMPOTENCY_HEADER);
        if (!StringUtils.hasText(rawKey)) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "幂等键不能为空");
        }

        Long userId = currentUserId();
        String bizId = resolveExpression(joinPoint, idempotent.bizId());
        String recordKey = idempotentService.buildRecordKey(idempotent.bizType(), userId, rawKey);
        String requestHash = buildRequestHash(joinPoint, request, idempotent.bizType(), bizId);
        IdempotentRecordPO record = idempotentService.tryCreateProcessing(
                recordKey,
                idempotent.bizType(),
                bizId,
                requestHash,
                LocalDateTime.now().plusSeconds(idempotent.expireSeconds()));

        if (record == null) {
            throw new BizException(CommonErrorCode.CONFLICT, "幂等请求处理异常，请稍后重试");
        }
        ensureRequestNotConflicting(record, requestHash);
        if (Boolean.TRUE.equals(record.getNewlyCreated())) {
            return proceedAndSave(joinPoint, record);
        }
        if (record.getStatus() == IdempotentStatus.SUCCESS && StringUtils.hasText(record.getResponseBody())) {
            return readHistoricalResponse(joinPoint, record.getResponseBody());
        }
        if (record.getStatus() == IdempotentStatus.PROCESSING && !isExpired(record)) {
            throw new BizException(CommonErrorCode.CONFLICT, "请求正在处理中，请稍后重试");
        }

        return proceedAndSave(joinPoint, record);
    }

    private Object proceedAndSave(ProceedingJoinPoint joinPoint, IdempotentRecordPO record) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            idempotentService.saveSuccessResponse(record.getId(), sensitiveFieldMasker.mask(objectMapper.writeValueAsString(result)));
            return result;
        } catch (Throwable ex) {
            idempotentService.saveFailure(record.getId(), sensitiveFieldMasker.mask(ex.getMessage()));
            throw ex;
        }
    }

    private Object readHistoricalResponse(ProceedingJoinPoint joinPoint, String responseBody) throws Exception {
        Type returnType = ((MethodSignature) joinPoint.getSignature()).getMethod().getGenericReturnType();
        if (returnType == null) {
            return objectMapper.readValue(responseBody, new TypeReference<Object>() {});
        }
        return objectMapper.readValue(responseBody, objectMapper.getTypeFactory().constructType(returnType));
    }

    private void ensureRequestNotConflicting(IdempotentRecordPO record, String requestHash) {
        if (StringUtils.hasText(record.getRequestHash())
                && StringUtils.hasText(requestHash)
                && !record.getRequestHash().equals(requestHash)
                && record.getStatus() == IdempotentStatus.SUCCESS) {
            throw new BizException(CommonErrorCode.CONFLICT, "幂等键已被不同请求使用");
        }
    }

    private boolean isExpired(IdempotentRecordPO record) {
        return record.getExpireTime() != null && record.getExpireTime().isBefore(LocalDateTime.now());
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }

    private String buildRequestHash(
            ProceedingJoinPoint joinPoint,
            HttpServletRequest request,
            String bizType,
            String bizId) {
        try {
            String method = request == null ? "" : request.getMethod();
            String path = request == null ? "" : request.getRequestURI();
            String args = objectMapper.writeValueAsString(joinPoint.getArgs());
            return sha256(method + "|" + path + "|" + bizType + "|" + bizId + "|" + args);
        } catch (Exception ex) {
            return sha256(String.valueOf(joinPoint.getSignature()) + "|" + bizType + "|" + bizId);
        }
    }

    private String resolveExpression(ProceedingJoinPoint joinPoint, String expression) {
        if (!StringUtils.hasText(expression)) {
            return null;
        }
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            EvaluationContext context = new StandardEvaluationContext();
            Object[] args = joinPoint.getArgs();
            String[] names = signature.getParameterNames();
            for (int i = 0; i < args.length; i++) {
                context.setVariable("p" + i, args[i]);
                context.setVariable("a" + i, args[i]);
                if (names != null && i < names.length) {
                    context.setVariable(names[i], args[i]);
                }
            }
            Object value = expressionParser.parseExpression(expression).getValue(context);
            return value == null ? null : String.valueOf(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("请求摘要生成失败", ex);
        }
    }
}
