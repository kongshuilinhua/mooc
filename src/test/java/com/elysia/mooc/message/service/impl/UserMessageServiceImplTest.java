package com.elysia.mooc.message.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.MessageType;
import com.elysia.mooc.common.enums.ReadStatus;
import com.elysia.mooc.message.domain.dto.MarkMessagesReadRequest;
import com.elysia.mooc.message.domain.dto.UserMessageQuery;
import com.elysia.mooc.message.domain.po.MessageReceiverPO;
import com.elysia.mooc.message.domain.vo.MessageVO;
import com.elysia.mooc.message.mapper.MessageMapper;
import com.elysia.mooc.message.mapper.MessageReceiverMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 用户消息服务真实表查询规则测试。 */
@ExtendWith(MockitoExtension.class)
class UserMessageServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private MessageReceiverMapper messageReceiverMapper;

    @InjectMocks
    private UserMessageServiceImpl userMessageService;

    @Test
    void getUnreadCountShouldCountCurrentUserActiveMessages() {
        when(userContextService.currentUserId()).thenReturn(3L);
        when(messageReceiverMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        Map<String, Integer> result = userMessageService.getUnreadCount();

        assertThat(result).containsEntry("unreadCount", 2);
        verify(messageReceiverMapper).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    void listMessagesShouldUseCurrentUserAndEnumFilters() {
        when(userContextService.currentUserId()).thenReturn(3L);
        Page<MessageVO> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(MessageVO.builder()
                .id(10001L)
                .type(MessageType.SYSTEM)
                .content("欢迎使用 MOOC 演示平台")
                .isRead(ReadStatus.UNREAD)
                .createTime(LocalDateTime.now())
                .build()));
        when(messageMapper.selectUserMessagePage(any(), eq(3L), eq(MessageType.SYSTEM), eq(ReadStatus.UNREAD)))
                .thenReturn(page);
        UserMessageQuery query = new UserMessageQuery();
        query.setType(MessageType.SYSTEM);
        query.setIsRead(ReadStatus.UNREAD);

        PageResult<MessageVO> result = userMessageService.listMessages(query);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getTotalPage()).isEqualTo(1);
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getList().get(0).getType()).isEqualTo(MessageType.SYSTEM);
        verify(messageMapper).selectUserMessagePage(any(), eq(3L), eq(MessageType.SYSTEM), eq(ReadStatus.UNREAD));
    }

    @Test
    void markReadShouldOnlyUpdateCurrentUserUnreadReceivers() {
        when(userContextService.currentUserId()).thenReturn(3L);
        MarkMessagesReadRequest request = new MarkMessagesReadRequest();
        request.setMessageIds(List.of(10001L, 10002L));

        Boolean result = userMessageService.markRead(request);

        assertThat(result).isTrue();
        ArgumentCaptor<MessageReceiverPO> updateCaptor = ArgumentCaptor.forClass(MessageReceiverPO.class);
        verify(messageReceiverMapper).update(updateCaptor.capture(), any());
        assertThat(updateCaptor.getValue().getReadStatus()).isEqualTo(ReadStatus.READ);
        assertThat(updateCaptor.getValue().getReadTime()).isNotNull();
    }
}
