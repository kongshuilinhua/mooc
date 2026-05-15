package com.elysia.mooc.knowledge.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeScopeType;
import java.time.LocalDateTime;
import lombok.Data;

/** 知识库实体，映射 knowledge_base 表。 */
@Data
@TableName("knowledge_base")
public class KnowledgeBasePO {

    /** 知识库 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 知识库名称。 */
    private String name;

    /** 知识库编码。 */
    private String code;

    /** 知识库说明。 */
    private String description;

    /** 范围类型。 */
    private KnowledgeScopeType scopeType;

    /** 绑定课程 ID。 */
    private Long courseId;

    /** 启停状态。 */
    private EnableStatus status;

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
