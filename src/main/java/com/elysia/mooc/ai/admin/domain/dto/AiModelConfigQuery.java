package com.elysia.mooc.ai.admin.domain.dto;

import com.elysia.mooc.ai.admin.domain.enums.AiModelScene;
import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.common.enums.EnableStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** AI 模型配置分页查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiModelConfigQuery extends PageQuery {

    /** 搜索关键字，匹配供应商、模型名、场景或 API Key 引用。 */
    private String keyword;

    /** 场景筛选。 */
    private AiModelScene scene;

    /** 启停状态筛选。 */
    private EnableStatus status;

    /** 兼容前端 enabled 筛选字段。 */
    private EnableStatus enabled;

    /** 排序字段白名单：id、createTime、updateTime、scene。 */
    private String sortBy;

    /** 是否升序。 */
    private Boolean isAsc = Boolean.FALSE;

    public EnableStatus resolvedEnabled() {
        return status == null ? enabled : status;
    }
}
