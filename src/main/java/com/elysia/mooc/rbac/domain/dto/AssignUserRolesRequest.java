package com.elysia.mooc.rbac.domain.dto;

import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.util.StringUtils;

/** 分配用户角色请求参数 */
@Data
public class AssignUserRolesRequest implements Checker {

    private static final int MAX_ROLE_ASSIGN_SIZE = 50;

    /** 角色ID列表，会替换用户当前所有角色 */
    private List<Long> roleIds;

    /** 角色编码列表，兼容前端直接提交编码的场景 */
    private List<String> roleCodes;

    @Override
    public void check() {
        this.roleIds = normalizeRoleIds(roleIds);
        this.roleCodes = normalizeRoleCodes(roleCodes);
        if (roleIds.isEmpty() && roleCodes.isEmpty()) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "角色ID列表和角色编码列表至少传一个");
        }
        if (roleIds.size() > MAX_ROLE_ASSIGN_SIZE || roleCodes.size() > MAX_ROLE_ASSIGN_SIZE) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "单次分配角色数量不能超过50个");
        }
    }

    private List<Long> normalizeRoleIds(List<Long> values) {
        if (values == null) {
            return Collections.emptyList();
        }
        List<Long> result = values.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (result.stream().anyMatch(id -> id <= 0)) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "角色ID必须为正数");
        }
        return result;
    }

    private List<String> normalizeRoleCodes(List<String> values) {
        if (values == null) {
            return Collections.emptyList();
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }
}
