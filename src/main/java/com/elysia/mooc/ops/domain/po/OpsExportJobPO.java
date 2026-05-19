package com.elysia.mooc.ops.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.ops.domain.enums.OpsExportJobStatus;
import com.elysia.mooc.ops.domain.enums.OpsExportType;
import java.time.LocalDateTime;
import lombok.Data;

/** 导出任务实体，映射 ops_export_job 表。 */
@Data
@TableName("ops_export_job")
public class OpsExportJobPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 导出任务类型。 */
    private OpsExportType jobType;

    /** 申请人 ID。 */
    private Long requestUserId;

    /** 导出参数 JSON 字符串。 */
    private String requestParams;

    /** 生成后的文件访问地址，异步完成前为空。 */
    private String fileUrl;

    /** 导出任务状态。 */
    private OpsExportJobStatus jobStatus;

    /** 完成时间。 */
    private LocalDateTime finishTime;

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
