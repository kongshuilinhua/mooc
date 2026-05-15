package com.elysia.mooc.event.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.event.domain.dto.EventConsumeLogQuery;
import com.elysia.mooc.event.domain.dto.EventPublishLogQuery;
import com.elysia.mooc.event.domain.vo.EventConsumeLogVO;
import com.elysia.mooc.event.domain.vo.EventPublishLogVO;

/** 管理端事件日志服务。 */
public interface EventAdminService {

    /**
     * 分页查询事件发布日志。
     *
     * @param query 查询条件
     * @return 发布日志分页
     */
    PageResult<EventPublishLogVO> listPublishLogs(EventPublishLogQuery query);

    /**
     * 分页查询事件消费日志。
     *
     * @param query 查询条件
     * @return 消费日志分页
     */
    PageResult<EventConsumeLogVO> listConsumeLogs(EventConsumeLogQuery query);

    /**
     * 手动重试事件。
     *
     * @param eventId 全局唯一事件 ID
     * @return 重试成功返回 true
     */
    Boolean retry(String eventId);
}
