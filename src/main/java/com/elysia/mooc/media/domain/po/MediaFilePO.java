package com.elysia.mooc.media.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.media.domain.enums.MediaBizType;
import com.elysia.mooc.media.domain.enums.MediaUploadStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 媒资文件实体，映射 media_file 表。 */
@Data
@TableName("media_file")
public class MediaFilePO {

    /** 文件 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 拥有者 ID。 */
    private Long ownerId;

    /** 业务类型。 */
    private MediaBizType bizType;

    /** 原文件名。 */
    private String originalName;

    /** 存储路径。 */
    private String storagePath;

    /** 访问地址。 */
    private String url;

    /** 内容类型。 */
    private String contentType;

    /** 文件大小。 */
    private Long fileSize;

    /** 文件摘要。 */
    private String fileHash;

    /** 上传状态。 */
    private MediaUploadStatus uploadStatus;

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
