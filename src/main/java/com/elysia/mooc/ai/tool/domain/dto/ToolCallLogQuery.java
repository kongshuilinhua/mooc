package com.elysia.mooc.ai.tool.domain.dto;

import com.elysia.mooc.ai.tool.domain.enums.ToolCallStatus;
import com.elysia.mooc.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Tool 调用日志分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ToolCallLogQuery extends PageQuery {

    /** 工具名称。 */
    private String toolName;

    /** 调用状态。 */
    private ToolCallStatus status;

    /** 搜索关键字，匹配工具名或错误信息。 */
    private String keyword;

    /** 排序字段，支持 createTime、costMs、id。 */
    private String sortBy;

    /** 是否升序，默认 false。 */
    private Boolean isAsc;
}
