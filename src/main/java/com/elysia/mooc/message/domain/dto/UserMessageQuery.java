package com.elysia.mooc.message.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.common.enums.MessageType;
import com.elysia.mooc.common.enums.ReadStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 用户消息分页查询参数 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserMessageQuery extends PageQuery {

    /** 消息类型，支持 SYSTEM/COURSE/AUDIT/AI */
    private MessageType type;

    /** 是否已读：0 未读，1 已读 */
    private ReadStatus isRead;
}
