package com.elysia.mooc.message.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.common.enums.ReadStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 消息接收记录实体，映射 message_receiver 表。 */
@Data
@TableName("message_receiver")
public class MessageReceiverPO {

    /** 接收记录 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息 ID。 */
    private Long messageId;

    /** 接收人 ID。 */
    private Long receiverId;

    /** 阅读状态。 */
    private ReadStatus readStatus;

    /** 已读时间。 */
    private LocalDateTime readTime;

    /** 逻辑删除标记。 */
    @TableLogic
    private Integer deleted;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
