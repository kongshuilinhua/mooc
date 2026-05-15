package com.elysia.mooc.knowledge.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeDocumentSourceType;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeProcessStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 知识库文档实体，映射 knowledge_document 表。 */
@Data
@TableName("knowledge_document")
public class KnowledgeDocumentPO {

    /** 文档 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 知识库 ID。 */
    private Long kbId;

    /** 媒资文件 ID。 */
    private Long mediaFileId;

    /** 文档标题。 */
    private String title;

    /** 来源类型。 */
    private KnowledgeDocumentSourceType sourceType;

    /** 来源地址。 */
    private String sourceUrl;

    /** 内容摘要。 */
    private String contentHash;

    /** 解析状态。 */
    private KnowledgeProcessStatus parseStatus;

    /** 解析错误。 */
    private String parseError;

    /** 切片数量。 */
    private Integer segmentCount;

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
