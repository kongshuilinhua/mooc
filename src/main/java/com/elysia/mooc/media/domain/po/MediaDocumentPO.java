package com.elysia.mooc.media.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.media.domain.enums.MediaParseStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 文档媒资实体，映射 media_document 表。 */
@Data
@TableName("media_document")
public class MediaDocumentPO {

    /** 文档 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 媒资文件 ID。 */
    private Long mediaFileId;

    /** 页数。 */
    private Integer pageCount;

    /** 解析状态。 */
    private MediaParseStatus parseStatus;

    /** 解析错误。 */
    private String parseError;

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
