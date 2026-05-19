package com.elysia.mooc.ops.domain.vo;

import com.elysia.mooc.ops.domain.enums.OpsExportJobStatus;
import com.elysia.mooc.ops.domain.enums.OpsExportType;
import java.time.LocalDateTime;
import lombok.Data;

/** 导出任务创建结果。 */
@Data
public class ExportTaskResultVO {

    /** 导出任务 ID。 */
    private Long exportId;

    /** 导出类型。 */
    private OpsExportType exportType;

    /** 任务状态。 */
    private OpsExportJobStatus status;

    /** 文件名，异步任务创建后先返回预计文件名。 */
    private String fileName;

    /** 文件地址，任务完成前为空。 */
    private String fileUrl;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 完成时间。 */
    private LocalDateTime finishedAt;
}
