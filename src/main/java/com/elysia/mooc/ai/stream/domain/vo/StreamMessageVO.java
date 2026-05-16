package com.elysia.mooc.ai.stream.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** SSE message 事件数据。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamMessageVO {

    /** 本次增量文本。 */
    private String content;
}
