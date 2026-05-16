package com.elysia.mooc.ai.chat.domain.vo;

import lombok.Data;

/** AI 回复引用来源，day15 普通聊天默认返回空列表。 */
@Data
public class AiSourceVO {

    /** 来源类型。 */
    private String sourceType;

    /** 来源 ID。 */
    private Long sourceId;

    /** 知识库 ID。 */
    private Long kbId;

    /** 文档 ID。 */
    private Long documentId;

    /** 切片 ID。 */
    private Long segmentId;

    /** 来源标题。 */
    private String title;

    /** 相似度得分。 */
    private Double score;

    /** 内容预览。 */
    private String preview;
}
