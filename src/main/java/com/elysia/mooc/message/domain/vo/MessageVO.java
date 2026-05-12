package com.elysia.mooc.message.domain.vo;

import com.elysia.mooc.common.enums.MessageType;
import com.elysia.mooc.common.enums.ReadStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/** 用户消息视图对象 */
@Data
@Builder
public class MessageVO {

    /** 消息ID */
    private Long id;

    /** 消息类型 */
    private MessageType type;

    /** 消息内容 */
    private String content;

    /** 是否已读：0 未读，1 已读 */
    private ReadStatus isRead;

    /** 创建时间 */
    private LocalDateTime createTime;
}
