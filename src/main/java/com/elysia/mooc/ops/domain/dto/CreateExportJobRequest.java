package com.elysia.mooc.ops.domain.dto;

import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import com.elysia.mooc.ops.domain.enums.OpsExportFormat;
import com.elysia.mooc.ops.domain.enums.OpsExportType;
import java.time.LocalDate;
import lombok.Data;

/** 创建导出任务请求。 */
@Data
public class CreateExportJobRequest implements Checker {

    /** 前端传入的导出类型。 */
    private OpsExportType exportType;

    /** 兼容后端语义的导出任务类型。 */
    private OpsExportType jobType;

    /** 业务日期。 */
    private LocalDate bizDate;

    /** 文件格式。 */
    private OpsExportFormat format = OpsExportFormat.XLSX;

    @Override
    public void check() {
        if (jobType == null) {
            jobType = exportType;
        }
        if (exportType == null) {
            exportType = jobType;
        }
        if (jobType == null) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "导出类型不能为空");
        }
        if (format == null) {
            format = OpsExportFormat.XLSX;
        }
    }
}
