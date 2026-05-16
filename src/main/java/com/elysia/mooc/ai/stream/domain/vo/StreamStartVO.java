package com.elysia.mooc.ai.stream.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** SSE start 事件数据。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamStartVO {

    /** 会话 ID。 */
    private Long conversationId;

    /** 助手消息 ID。 */
    private Long messageId;

    /** 会话场景。 */
    private String scene;
}
