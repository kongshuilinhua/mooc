package com.elysia.mooc.media.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.media.domain.enums.MediaTranscodeStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 视频媒资实体，映射 media_video 表。 */
@Data
@TableName("media_video")
public class MediaVideoPO {

    /** 视频 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 媒资文件 ID。 */
    private Long mediaFileId;

    /** 时长，单位秒。 */
    private Integer durationSeconds;

    /** 视频宽度。 */
    private Integer width;

    /** 视频高度。 */
    private Integer height;

    /** 转码状态。 */
    private MediaTranscodeStatus transcodeStatus;

    /** 封面地址。 */
    private String coverUrl;

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
