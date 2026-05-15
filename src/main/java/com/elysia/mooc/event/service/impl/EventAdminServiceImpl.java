package com.elysia.mooc.event.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.event.domain.dto.EventConsumeLogQuery;
import com.elysia.mooc.event.domain.dto.EventPublishLogQuery;
import com.elysia.mooc.event.domain.po.EventConsumeLogPO;
import com.elysia.mooc.event.domain.po.EventPublishLogPO;
import com.elysia.mooc.event.domain.vo.EventConsumeLogVO;
import com.elysia.mooc.event.domain.vo.EventPublishLogVO;
import com.elysia.mooc.event.mapper.EventConsumeLogMapper;
import com.elysia.mooc.event.mapper.EventPublishLogMapper;
import com.elysia.mooc.event.service.EventAdminService;
import com.elysia.mooc.event.service.EventPublisher;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 管理端事件日志服务实现。 */
@Service
@RequiredArgsConstructor
public class EventAdminServiceImpl implements EventAdminService {

    private static final Map<String, SFunction<EventPublishLogPO, ?>> PUBLISH_SORT_FIELDS = Map.of(
            "id", EventPublishLogPO::getId,
            "createTime", EventPublishLogPO::getCreateTime,
            "updateTime", EventPublishLogPO::getUpdateTime,
            "retryCount", EventPublishLogPO::getRetryCount,
            "nextRetryTime", EventPublishLogPO::getNextRetryTime);

    private static final Map<String, SFunction<EventConsumeLogPO, ?>> CONSUME_SORT_FIELDS = Map.of(
            "id", EventConsumeLogPO::getId,
            "createTime", EventConsumeLogPO::getCreateTime);

    private final EventPublishLogMapper eventPublishLogMapper;
    private final EventConsumeLogMapper eventConsumeLogMapper;
    private final EventPublisher eventPublisher;

    /**
     * 分页查询事件发布日志。
     *
     * @param query 查询条件
     * @return 发布日志分页
     */
    @Override
    public PageResult<EventPublishLogVO> listPublishLogs(EventPublishLogQuery query) {
        EventPublishLogQuery safeQuery = query == null ? new EventPublishLogQuery() : query;
        LambdaQueryWrapper<EventPublishLogPO> wrapper = new LambdaQueryWrapper<>();
        if (safeQuery.getStatus() != null) {
            wrapper.eq(EventPublishLogPO::getStatus, safeQuery.getStatus());
        }
        if (StringUtils.hasText(safeQuery.getEventType())) {
            wrapper.eq(EventPublishLogPO::getEventType, safeQuery.getEventType().trim());
        }
        if (StringUtils.hasText(safeQuery.getTopic())) {
            wrapper.eq(EventPublishLogPO::getTopic, safeQuery.getTopic().trim());
        }
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            String keyword = safeQuery.getKeyword().trim();
            wrapper.and(w -> w.like(EventPublishLogPO::getEventId, keyword)
                    .or().like(EventPublishLogPO::getTopic, keyword)
                    .or().like(EventPublishLogPO::getEventType, keyword)
                    .or().like(EventPublishLogPO::getBizKey, keyword)
                    .or().like(EventPublishLogPO::getErrorMessage, keyword));
        }
        applyPublishOrder(wrapper, safeQuery);
        Page<EventPublishLogPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        return PageResult.of(eventPublishLogMapper.selectPage(page, wrapper),
                source -> BeanCopyUtils.copyBean(source, EventPublishLogVO.class));
    }

    /**
     * 分页查询事件消费日志。
     *
     * @param query 查询条件
     * @return 消费日志分页
     */
    @Override
    public PageResult<EventConsumeLogVO> listConsumeLogs(EventConsumeLogQuery query) {
        EventConsumeLogQuery safeQuery = query == null ? new EventConsumeLogQuery() : query;
        LambdaQueryWrapper<EventConsumeLogPO> wrapper = new LambdaQueryWrapper<>();
        if (safeQuery.getStatus() != null) {
            wrapper.eq(EventConsumeLogPO::getStatus, safeQuery.getStatus());
        }
        if (StringUtils.hasText(safeQuery.getEventId())) {
            wrapper.eq(EventConsumeLogPO::getEventId, safeQuery.getEventId().trim());
        }
        if (StringUtils.hasText(safeQuery.getConsumerGroup())) {
            wrapper.eq(EventConsumeLogPO::getConsumerGroup, safeQuery.getConsumerGroup().trim());
        }
        if (StringUtils.hasText(safeQuery.getTopic())) {
            wrapper.eq(EventConsumeLogPO::getTopic, safeQuery.getTopic().trim());
        }
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            String keyword = safeQuery.getKeyword().trim();
            wrapper.and(w -> w.like(EventConsumeLogPO::getEventId, keyword)
                    .or().like(EventConsumeLogPO::getTopic, keyword)
                    .or().like(EventConsumeLogPO::getConsumerGroup, keyword)
                    .or().like(EventConsumeLogPO::getErrorMessage, keyword));
        }
        applyConsumeOrder(wrapper, safeQuery);
        Page<EventConsumeLogPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        return PageResult.of(eventConsumeLogMapper.selectPage(page, wrapper),
                source -> BeanCopyUtils.copyBean(source, EventConsumeLogVO.class));
    }

    /**
     * 手动重试事件。
     *
     * @param eventId 全局唯一事件 ID
     * @return 重试成功返回 true
     */
    @Override
    public Boolean retry(String eventId) {
        return eventPublisher.retry(eventId);
    }

    private void applyPublishOrder(LambdaQueryWrapper<EventPublishLogPO> wrapper, EventPublishLogQuery query) {
        String sortBy = StringUtils.hasText(query.getSortBy()) ? query.getSortBy() : "createTime";
        SFunction<EventPublishLogPO, ?> sortField = PUBLISH_SORT_FIELDS.getOrDefault(
                sortBy, EventPublishLogPO::getCreateTime);
        boolean asc = Boolean.TRUE.equals(query.getIsAsc());
        wrapper.orderBy(true, asc, sortField);
        if (!"id".equals(sortBy)) {
            wrapper.orderByDesc(EventPublishLogPO::getId);
        }
    }

    private void applyConsumeOrder(LambdaQueryWrapper<EventConsumeLogPO> wrapper, EventConsumeLogQuery query) {
        String sortBy = StringUtils.hasText(query.getSortBy()) ? query.getSortBy() : "createTime";
        SFunction<EventConsumeLogPO, ?> sortField = CONSUME_SORT_FIELDS.getOrDefault(
                sortBy, EventConsumeLogPO::getCreateTime);
        boolean asc = Boolean.TRUE.equals(query.getIsAsc());
        wrapper.orderBy(true, asc, sortField);
        if (!"id".equals(sortBy)) {
            wrapper.orderByDesc(EventConsumeLogPO::getId);
        }
    }
}
