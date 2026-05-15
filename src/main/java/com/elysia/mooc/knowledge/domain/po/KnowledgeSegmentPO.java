package com.elysia.mooc.knowledge.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeProcessStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 知识切片实体，映射 knowledge_segment 表。 */
@Data
@TableName("knowledge_segment")
public class KnowledgeSegmentPO {

    /** 切片 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 知识库 ID。 */
    private Long kbId;

    /** 文档 ID。 */
    private Long documentId;

    /** 切片序号。 */
    private Integer segmentIndex;

    /** 切片标题。 */
    private String title;

    /** 切片内容。 */
    private String content;

    /** token 数量。 */
    private Integer tokenCount;

    /** JSON 扩展元数据。 */
    private String metadata;

    /** Qdrant 向量 ID。 */
    private String vectorId;

    /** 向量化状态。 */
    private KnowledgeProcessStatus embeddingStatus;

    /** 向量化错误。 */
    private String embeddingError;

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
