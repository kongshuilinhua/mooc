package com.elysia.mooc.statistics.service;

/** 搜索日志服务。 */
public interface SearchLogService {

    /**
     * 记录用户搜索日志，写入失败不影响主查询链路。
     *
     * @param keyword     搜索关键词
     * @param resultCount 结果数量
     */
    void recordSearch(String keyword, int resultCount);
}
