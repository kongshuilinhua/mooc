package com.elysia.mooc.message.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.MessageType;
import com.elysia.mooc.common.enums.ReadStatus;
import com.elysia.mooc.common.enums.StringToBaseEnumConverterFactory;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.elysia.mooc.common.validate.CheckerAspect;
import com.elysia.mooc.message.domain.dto.UserMessageQuery;
import com.elysia.mooc.message.domain.vo.MessageVO;
import com.elysia.mooc.message.service.UserMessageService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** 用户消息接口 HTTP 合同测试。 */
@ExtendWith(MockitoExtension.class)
class UserMessageControllerWebTest {

    @Mock
    private UserMessageService userMessageService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserMessageController controller = proxiedController(new UserMessageController(userMessageService));
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverterFactory(new StringToBaseEnumConverterFactory());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setConversionService(conversionService)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void unreadCountShouldReturnContract() throws Exception {
        when(userMessageService.getUnreadCount()).thenReturn(Map.of("unreadCount", 2));

        mockMvc.perform(get("/api/users/me/messages/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.unreadCount").value(2));
    }

    @Test
    void listMessagesShouldReturnPageResultAndAcceptEnumName() throws Exception {
        when(userMessageService.listMessages(any()))
                .thenReturn(PageResult.of(1L, 10, List.of(MessageVO.builder()
                        .id(10001L)
                        .type(MessageType.SYSTEM)
                        .content("欢迎使用 MOOC 演示平台")
                        .isRead(ReadStatus.UNREAD)
                        .createTime(LocalDateTime.now())
                        .build())));

        mockMvc.perform(get("/api/users/me/messages")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("type", "SYSTEM")
                        .param("isRead", "UNREAD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.totalPage").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(10001))
                .andExpect(jsonPath("$.data.list[0].type").value("SYSTEM"))
                .andExpect(jsonPath("$.data.list[0].isRead").value(0));

        ArgumentCaptor<UserMessageQuery> captor = ArgumentCaptor.forClass(UserMessageQuery.class);
        verify(userMessageService).listMessages(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getIsRead()).isEqualTo(ReadStatus.UNREAD);
    }

    @Test
    void listMessagesShouldReturn400WhenTypeInvalid() throws Exception {
        mockMvc.perform(get("/api/users/me/messages")
                        .param("type", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数类型错误或枚举值不合法"));
    }

    @Test
    void markReadShouldRejectEmptyMessageIds() throws Exception {
        mockMvc.perform(patch("/api/users/me/messages/read-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"messageIds":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("消息ID列表不能为空"));
    }

    @Test
    void markReadShouldRejectNegativeMessageId() throws Exception {
        mockMvc.perform(patch("/api/users/me/messages/read-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"messageIds":[10001,-1]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("消息ID必须为正数"));
    }

    @Test
    void markReadShouldReturnBooleanContract() throws Exception {
        when(userMessageService.markRead(any())).thenReturn(true);

        mockMvc.perform(patch("/api/users/me/messages/read-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"messageIds":[10001,10002,10002]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    private UserMessageController proxiedController(UserMessageController controller) {
        AspectJProxyFactory factory = new AspectJProxyFactory(controller);
        factory.addAspect(new CheckerAspect());
        return factory.getProxy();
    }
}
