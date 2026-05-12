package com.elysia.mooc.message.domain.dto;

import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

/** 批量标记已读请求参数 */
@Data
public class MarkMessagesReadRequest implements Checker {

    private static final int MAX_MARK_READ_SIZE = 100;

    /** 需要标记为已读的消息ID列表 */
    @NotEmpty(message = "消息ID列表不能为空")
    private List<Long> messageIds;

    @Override
    public void check() {
        if (messageIds == null || messageIds.isEmpty()) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "消息ID列表不能为空");
        }
        messageIds = messageIds.stream().distinct().toList();
        if (messageIds.size() > MAX_MARK_READ_SIZE) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "单次标记消息数量不能超过100条");
        }
        if (messageIds.stream().anyMatch(messageId -> messageId == null || messageId <= 0)) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "消息ID必须为正数");
        }
    }
}
