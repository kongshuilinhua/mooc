package com.elysia.mooc.common.trace;

/**
 * traceId 线程上下文，用于在一次请求内共享链路追踪 ID。
 */
public final class TraceContext {

    /** 当前线程保存的 traceId。 */
    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    /**
     * 工具类禁止实例化。
     */
    private TraceContext() {
    }

    /**
     * 设置当前请求 traceId。
     *
     * @param traceId 链路追踪 ID
     */
    public static void setTraceId(String traceId) {
        TRACE_ID.set(traceId);
    }

    /**
     * 获取当前请求 traceId。
     *
     * @return 链路追踪 ID
     */
    public static String getTraceId() {
        return TRACE_ID.get();
    }

    /**
     * 清理当前线程 traceId，避免线程复用造成串号。
     */
    public static void clear() {
        TRACE_ID.remove();
    }
}
