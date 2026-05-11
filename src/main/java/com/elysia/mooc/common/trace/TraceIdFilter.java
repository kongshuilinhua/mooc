package com.elysia.mooc.common.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 为每个 HTTP 请求生成或透传 traceId，并写入响应头、线程上下文和日志 MDC。
 */
@Component
public class TraceIdFilter extends OncePerRequestFilter {

    /** 请求和响应中传递 traceId 的 Header 名称。 */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /** 日志 MDC 中保存 traceId 的键名。 */
    public static final String TRACE_ID_MDC_KEY = "traceId";

    /**
     * 处理请求级 traceId 生命周期。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException IO 异常
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        try {
            TraceContext.setTraceId(traceId);
            MDC.put(TRACE_ID_MDC_KEY, traceId);
            response.setHeader(TRACE_ID_HEADER, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_MDC_KEY);
            TraceContext.clear();
        }
    }
}
