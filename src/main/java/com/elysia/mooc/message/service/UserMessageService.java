package com.elysia.mooc.message.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.message.domain.dto.MarkMessagesReadRequest;
import com.elysia.mooc.message.domain.dto.UserMessageQuery;
import com.elysia.mooc.message.domain.vo.MessageVO;
import java.util.Map;

/** 用户消息服务 */
public interface UserMessageService {

    /** 查询当前用户未读消息数 */
    Map<String, Integer> getUnreadCount();

    /** 分页查询当前用户消息列表 */
    PageResult<MessageVO> listMessages(UserMessageQuery query);

    /** 批量标记当前用户消息为已读 */
    boolean markRead(MarkMessagesReadRequest request);
}
