package com.elysia.mooc.rbac.domain.vo;

import com.elysia.mooc.common.enums.EnableStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** 管理员视角的用户视图对象，包含角色信息 */
@Data
@Builder
public class AdminUserVO {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 状态：0 禁用，1 启用 */
    private EnableStatus status;

    /** 角色编码列表 */
    private List<String> roleCodes;

    /** 兼容前端当前字段名的角色编码列表 */
    private List<String> roles;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 最后登录IP */
    private String lastLoginIp;

    /** 注册时间 */
    private LocalDateTime createTime;
}
