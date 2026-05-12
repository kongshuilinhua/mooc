package com.elysia.mooc.message.service.impl;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.MessageType;
import com.elysia.mooc.common.enums.ReadStatus;
import com.elysia.mooc.message.domain.dto.MarkMessagesReadRequest;
import com.elysia.mooc.message.domain.dto.UserMessageQuery;
import com.elysia.mooc.message.domain.vo.MessageVO;
import com.elysia.mooc.message.service.UserMessageService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户消息服务默认实现。
 * 当前项目尚未接入正式消息表时，先提供稳定的演示数据以保证前端链路可用。
 */
@Service
@RequiredArgsConstructor
public class UserMessageServiceImpl implements UserMessageService {

    private final Map<Long, ReadStatus> readStatusStore = new ConcurrentHashMap<>();

    /**
     * 查询当前用户未读消息数。
     * @return 未读数量
     */
    @Override
    public Map<String, Integer> getUnreadCount() {
        // 1. 复用当前演示消息集统计未读数，避免页面加载时因接口缺失报错
        int unreadCount = (int) buildDemoMessages().stream()
                .filter(message -> ReadStatus.UNREAD == message.getIsRead())
                .count();
        return Map.of("unreadCount", unreadCount);
    }

    /**
     * 分页查询当前用户消息。
     * @param query 分页筛选参数
     * @return 分页结果
     */
    @Override
    public PageResult<MessageVO> listMessages(UserMessageQuery query) {
        // 1. 基于最小演示数据实现分页和已读筛选，保证消息中心能正常展示
        List<MessageVO> filtered = buildDemoMessages().stream()
                .filter(message -> query.getType() == null || query.getType().equals(message.getType()))
                .filter(message -> query.getIsRead() == null || query.getIsRead().equals(message.getIsRead()))
                .collect(Collectors.toList());

        int pageNo = query.getPageNo();
        int pageSize = query.getPageSize();
        int fromIndex = Math.max(0, (pageNo - 1) * pageSize);
        int toIndex = Math.min(filtered.size(), fromIndex + pageSize);
        List<MessageVO> pageList = fromIndex >= filtered.size() ? List.of() : filtered.subList(fromIndex, toIndex);
        return PageResult.of((long) filtered.size(), pageSize, pageList);
    }

    /**
     * 批量标记已读。
     * @param request 消息ID列表
     * @return 是否成功
     */
    @Override
    public boolean markRead(MarkMessagesReadRequest request) {
        // 1. 仅记录本地已读状态，满足当前前端交互闭环，后续接真实消息表时再替换
        request.getMessageIds().forEach(messageId -> readStatusStore.put(messageId, ReadStatus.READ));
        return true;
    }

    /**
     * 构造当前用户可见的演示消息。
     * @return 消息列表
     */
    private List<MessageVO> buildDemoMessages() {
        // 1. 保持与前端演示文案一致，避免页面突然空白
        return List.of(
                MessageVO.builder()
                        .id(1L)
                        .type(MessageType.SYSTEM)
                        .content("欢迎进入课程学习平台，当前账号的系统通知与课程提醒会显示在这里。")
                        .isRead(readStatusStore.getOrDefault(1L, ReadStatus.UNREAD))
                        .createTime(LocalDateTime.now().minusHours(2))
                        .build(),
                MessageVO.builder()
                        .id(2L)
                        .type(MessageType.COURSE)
                        .content("用户管理与角色分配接口已接入后端，可继续联调真实业务流程。")
                        .isRead(readStatusStore.getOrDefault(2L, ReadStatus.UNREAD))
                        .createTime(LocalDateTime.now().minusMinutes(30))
                        .build());
    }
}
