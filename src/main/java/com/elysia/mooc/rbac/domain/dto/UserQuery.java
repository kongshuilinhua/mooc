package com.elysia.mooc.rbac.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.common.enums.EnableStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户列表查询参数，继承通用分页参数。
 * 支持按关键字搜索和状态筛选。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends PageQuery {

    /** 搜索关键字，匹配用户名、昵称、邮箱 */
    private String keyword;

    /** 状态筛选：1/ENABLED 启用，0/DISABLED 禁用 */
    private EnableStatus status;
}
