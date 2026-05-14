package com.elysia.mooc.message.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.common.enums.MessageType;
import java.time.LocalDateTime;
import lombok.Data;

/** 站内消息实体，映射 message 表。 */
@Data
@TableName("message")
public class MessagePO {

    /** 消息 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送人 ID，系统消息允许为空。 */
    private Long senderId;

    /** 消息类型。 */
    @TableField("message_type")
    private MessageType type;

    /** 消息标题。 */
    private String title;

    /** 消息正文。 */
    private String content;

    /** 跳转地址。 */
    private String linkUrl;

    /** 创建时间。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人 ID。 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 更新人 ID。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 逻辑删除标记。 */
    @TableLogic
    private Integer deleted;
}
