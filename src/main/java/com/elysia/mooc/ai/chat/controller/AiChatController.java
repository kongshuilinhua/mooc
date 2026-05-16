package com.elysia.mooc.ai.chat.controller;

import com.elysia.mooc.ai.chat.domain.dto.AiConversationQuery;
import com.elysia.mooc.ai.chat.domain.dto.ChatRequest;
import com.elysia.mooc.ai.chat.domain.vo.ChatResultVO;
import com.elysia.mooc.ai.chat.domain.vo.ConversationDetailVO;
import com.elysia.mooc.ai.chat.domain.vo.ConversationVO;
import com.elysia.mooc.ai.chat.service.AiChatService;
import com.elysia.mooc.ai.chat.service.AiConversationService;
import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** AI 普通聊天接口。 */
@Tag(name = "AI 普通聊天")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiChatService aiChatService;
    private final AiConversationService aiConversationService;

    /**
     * 普通聊天。
     *
     * @param request 聊天请求
     * @return AI 回复
     */
    @Operation(summary = "普通聊天")
    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<ChatResultVO> chat(@Valid @RequestBody ChatRequest request) {
        return ApiResult.ok(aiChatService.chat(request));
    }

    /**
     * 分页查询本人 AI 会话。
     *
     * @param query 查询条件
     * @return 会话分页
     */
    @Operation(summary = "分页查询本人 AI 会话")
    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<PageResult<ConversationVO>> listConversations(@Valid AiConversationQuery query) {
        return ApiResult.ok(aiConversationService.listConversations(query));
    }

    /**
     * 查询本人 AI 会话详情。
     *
     * @param id 会话 ID
     * @return 会话详情
     */
    @Operation(summary = "查询本人 AI 会话详情")
    @GetMapping("/conversations/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<ConversationDetailVO> getConversation(@PathVariable Long id) {
        return ApiResult.ok(aiConversationService.getConversation(id));
    }

    /**
     * 删除本人 AI 会话。
     *
     * @param id 会话 ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除本人 AI 会话")
    @DeleteMapping("/conversations/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<Boolean> deleteConversation(@PathVariable Long id) {
        return ApiResult.ok(aiConversationService.deleteConversation(id));
    }
}
