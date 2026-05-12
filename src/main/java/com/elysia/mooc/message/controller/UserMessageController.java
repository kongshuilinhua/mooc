package com.elysia.mooc.message.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.validate.ParamChecker;
import com.elysia.mooc.message.domain.dto.MarkMessagesReadRequest;
import com.elysia.mooc.message.domain.dto.UserMessageQuery;
import com.elysia.mooc.message.domain.vo.MessageVO;
import com.elysia.mooc.message.service.UserMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 当前登录用户的消息中心接口。
 */
@Tag(name = "用户消息中心")
@RestController
@RequestMapping("/api/users/me/messages")
@RequiredArgsConstructor
public class UserMessageController {

    private final UserMessageService userMessageService;

    /**
     * 查询当前用户未读消息数。
     * @return 未读数
     */
    @Operation(summary = "查询未读消息数")
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<Map<String, Integer>> getUnreadCount() {
        return ApiResult.ok(userMessageService.getUnreadCount());
    }

    /**
     * 分页查询当前用户消息列表。
     * @param query 分页查询参数
     * @return 消息分页结果
     */
    @Operation(summary = "分页查询消息列表")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResult<PageResult<MessageVO>> listMessages(@Valid UserMessageQuery query) {
        return ApiResult.ok(userMessageService.listMessages(query));
    }

    /**
     * 批量标记消息已读。
     * @param request 消息ID列表
     * @return 是否成功
     */
    @Operation(summary = "批量标记消息已读")
    @PatchMapping("/read-status")
    @PreAuthorize("isAuthenticated()")
    @ParamChecker
    public ApiResult<Boolean> markRead(@Valid @RequestBody MarkMessagesReadRequest request) {
        return ApiResult.ok(userMessageService.markRead(request));
    }
}
