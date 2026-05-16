package com.elysia.mooc.ai.admin.domain.vo;

import java.util.List;
import lombok.Data;

/** 知识库文档处理状态统计。 */
@Data
public class DocumentStatusOverviewVO {

    /** 文档总数。 */
    private Long totalDocuments;

    /** 解析待处理数量。 */
    private Long parsePending;

    /** 解析处理中数量。 */
    private Long parseProcessing;

    /** 解析成功数量。 */
    private Long parseSuccess;

    /** 解析失败数量。 */
    private Long parseFailed;

    /** 向量化待处理数量。 */
    private Long embeddingPending;

    /** 向量化处理中数量。 */
    private Long embeddingProcessing;

    /** 向量化成功数量。 */
    private Long embeddingSuccess;

    /** 向量化失败数量。 */
    private Long embeddingFailed;

    /** 切片总数。 */
    private Long totalSegments;

    /** 解析状态分布。 */
    private List<DocumentStatusCountVO> parseStatusCounts;

    /** 向量化状态分布。 */
    private List<DocumentStatusCountVO> embeddingStatusCounts;
}
