package com.elysia.mooc.ai.chat.service;

import com.elysia.mooc.ai.chat.domain.dto.AiConversationQuery;
import com.elysia.mooc.ai.chat.domain.vo.ConversationDetailVO;
import com.elysia.mooc.ai.chat.domain.vo.ConversationVO;
import com.elysia.mooc.common.api.PageResult;

/** AI 会话管理服务。 */
public interface AiConversationService {

    /**
     * 分页查询当前用户会话。
     *
     * @param query 查询条件
     * @return 会话分页
     */
    PageResult<ConversationVO> listConversations(AiConversationQuery query);

    /**
     * 查询当前用户会话详情。
     *
     * @param id 会话 ID
     * @return 会话详情
     */
    ConversationDetailVO getConversation(Long id);

    /**
     * 删除当前用户会话。
     *
     * @param id 会话 ID
     * @return 是否删除成功
     */
    Boolean deleteConversation(Long id);
}
