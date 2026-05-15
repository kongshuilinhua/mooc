package com.elysia.mooc.message.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.elysia.mooc.common.enums.MessageType;
import com.elysia.mooc.common.enums.ReadStatus;
import com.elysia.mooc.message.domain.po.MessagePO;
import com.elysia.mooc.message.domain.po.MessageReceiverPO;
import com.elysia.mooc.message.mapper.MessageMapper;
import com.elysia.mooc.message.mapper.MessageReceiverMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 站内信写入服务测试。 */
@ExtendWith(MockitoExtension.class)
class MessageCommandServiceImplTest {

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private MessageReceiverMapper messageReceiverMapper;

    @InjectMocks
    private MessageCommandServiceImpl messageCommandService;

    @Test
    void sendToUserShouldInsertMessageAndUnreadReceiver() {
        Long messageId = messageCommandService.sendToUser(
                1L,
                2L,
                MessageType.AUDIT,
                " 审核通过 ",
                " 课程已发布 ",
                "/teacher/courses/3001");

        assertThat(messageId).isNull();
        ArgumentCaptor<MessagePO> messageCaptor = ArgumentCaptor.forClass(MessagePO.class);
        verify(messageMapper).insert(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getType()).isEqualTo(MessageType.AUDIT);
        assertThat(messageCaptor.getValue().getTitle()).isEqualTo("审核通过");
        assertThat(messageCaptor.getValue().getContent()).isEqualTo("课程已发布");
        assertThat(messageCaptor.getValue().getDeleted()).isZero();

        ArgumentCaptor<MessageReceiverPO> receiverCaptor = ArgumentCaptor.forClass(MessageReceiverPO.class);
        verify(messageReceiverMapper).insert(receiverCaptor.capture());
        assertThat(receiverCaptor.getValue().getReceiverId()).isEqualTo(2L);
        assertThat(receiverCaptor.getValue().getReadStatus()).isEqualTo(ReadStatus.UNREAD);
        assertThat(receiverCaptor.getValue().getCreateTime()).isNotNull();
        assertThat(receiverCaptor.getValue().getDeleted()).isZero();
    }
}
