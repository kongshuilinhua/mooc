package com.elysia.mooc.common.audit;

import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.common.audit.domain.po.OpsAuditLogPO;
import com.elysia.mooc.common.audit.service.AuditLogService;
import com.elysia.mooc.common.trace.TraceContext;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
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

/** 审计日志切面。 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;
    private final SensitiveFieldMasker sensitiveFieldMasker;
    private final SpelExpressionParser expressionParser = new SpelExpressionParser();

    /**
     * 围绕标记了审计注解的方法记录执行结果。
     *
     * @param joinPoint 切点
     * @param auditLog  审计注解
     * @return 原方法返回值
     * @throws Throwable 原方法异常
     */
    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            save(joinPoint, auditLog, true, null, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            save(joinPoint, auditLog, false, ex.getMessage(), System.currentTimeMillis() - start);
            throw ex;
        }
    }

    private void save(
            ProceedingJoinPoint joinPoint,
            AuditLog auditLog,
            boolean success,
            String errorMessage,
            long costMs) {
        OpsAuditLogPO entity = new OpsAuditLogPO();
        entity.setOperatorId(currentUserId());
        entity.setOperatorName(currentUsername());
        entity.setAction(auditLog.action());
        entity.setTargetType(auditLog.targetType());
        entity.setTargetId(resolveExpression(joinPoint, auditLog.targetId()));
        fillRequestInfo(entity);
        entity.setTraceId(TraceContext.getTraceId());
        entity.setSuccess(success ? 1 : 0);
        entity.setErrorMessage(sensitiveFieldMasker.mask(errorMessage));
        entity.setCostMs(costMs > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) costMs);
        entity.setCreateTime(LocalDateTime.now());
        auditLogService.saveQuietly(entity);
    }

    private void fillRequestInfo(OpsAuditLogPO entity) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        entity.setRequestMethod(request.getMethod());
        entity.setRequestPath(request.getRequestURI());
        entity.setRequestIp(resolveClientIp(request));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return StringUtils.hasText(realIp) ? realIp.trim() : request.getRemoteAddr();
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

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUsername();
        }
        return authentication.getName();
    }
}
