package com.elysia.mooc.ai.chat.domain.vo;

import com.elysia.mooc.ai.chat.domain.enums.AiConversationScene;
import java.time.LocalDateTime;
import lombok.Data;

/** AI 会话响应对象。 */
@Data
public class ConversationVO {

    /** 会话 ID。 */
    private Long id;

    /** 会话标题。 */
    private String title;

    /** 会话场景。 */
    private AiConversationScene scene;

    /** 绑定知识库 ID。 */
    private Long kbId;

    /** 绑定课程 ID。 */
    private Long courseId;

    /** 最后一条消息时间。 */
    private LocalDateTime lastMessageTime;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
