package com.elysia.mooc.event.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.event.domain.dto.EventConsumeLogQuery;
import com.elysia.mooc.event.domain.dto.EventPublishLogQuery;
import com.elysia.mooc.event.domain.enums.EventConsumeStatus;
import com.elysia.mooc.event.domain.enums.EventPublishStatus;
import com.elysia.mooc.event.domain.po.EventConsumeLogPO;
import com.elysia.mooc.event.domain.po.EventPublishLogPO;
import com.elysia.mooc.event.domain.vo.EventConsumeLogVO;
import com.elysia.mooc.event.domain.vo.EventPublishLogVO;
import com.elysia.mooc.event.mapper.EventConsumeLogMapper;
import com.elysia.mooc.event.mapper.EventPublishLogMapper;
import com.elysia.mooc.event.service.EventPublisher;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 管理端事件日志服务测试。 */
@ExtendWith(MockitoExtension.class)
class EventAdminServiceImplTest {

    @Mock
    private EventPublishLogMapper eventPublishLogMapper;

    @Mock
    private EventConsumeLogMapper eventConsumeLogMapper;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private EventAdminServiceImpl eventAdminService;

    @Test
    void listPublishLogsShouldReturnPageResultAndWhitelistSort() {
        EventPublishLogPO log = new EventPublishLogPO();
        log.setId(1L);
        log.setEventId("evt-1");
        log.setTopic("mooc.course.published");
        log.setEventType("COURSE_PUBLISHED");
        log.setStatus(EventPublishStatus.SENT);
        Page<EventPublishLogPO> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(log));
        when(eventPublishLogMapper.selectPage(any(), any())).thenReturn(page);
        EventPublishLogQuery query = new EventPublishLogQuery();
        query.setStatus(EventPublishStatus.SENT);
        query.setSortBy("非法字段");

        PageResult<EventPublishLogVO> result = eventAdminService.listPublishLogs(query);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getList()).first().extracting(EventPublishLogVO::getEventId).isEqualTo("evt-1");
        verify(eventPublishLogMapper).selectPage(any(), any());
    }

    @Test
    void listConsumeLogsShouldReturnPageResult() {
        EventConsumeLogPO log = new EventConsumeLogPO();
        log.setId(2L);
        log.setEventId("evt-2");
        log.setTopic("mooc.interaction.answer.created");
        log.setConsumerGroup("message-center");
        log.setStatus(EventConsumeStatus.SUCCESS);
        Page<EventConsumeLogPO> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(log));
        when(eventConsumeLogMapper.selectPage(any(), any())).thenReturn(page);
        EventConsumeLogQuery query = new EventConsumeLogQuery();
        query.setStatus(EventConsumeStatus.SUCCESS);

        PageResult<EventConsumeLogVO> result = eventAdminService.listConsumeLogs(query);

        assertThat(result.getTotalPage()).isEqualTo(1);
        assertThat(result.getList()).first().extracting(EventConsumeLogVO::getConsumerGroup)
                .isEqualTo("message-center");
    }

    @Test
    void retryShouldDelegateToEventPublisher() {
        when(eventPublisher.retry("evt-retry")).thenReturn(true);

        Boolean result = eventAdminService.retry("evt-retry");

        assertThat(result).isTrue();
        verify(eventPublisher).retry("evt-retry");
    }
}
