package com.elysia.mooc.knowledge.qdrant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Qdrant payload，保存向量命中后回溯业务主键所需的信息。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QdrantPointPayload {

    /** 知识库 ID。 */
    private Long kbId;

    /** 文档 ID。 */
    private Long documentId;

    /** 切片 ID。 */
    private Long segmentId;

    /** 课程 ID，可为空。 */
    private Long courseId;

    /** 切片序号。 */
    private Integer segmentIndex;

    /** 切片标题。 */
    private String title;

    /** 文档来源类型。 */
    private String sourceType;

    /** 切片正文。 */
    private String content;

    /** 切片扩展元数据。 */
    private String metadata;
}
