package com.elysia.mooc.auth.domain.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * 当前登录用户响应对象。
 */
@Data
@Builder
public class CurrentUserVO {

    /** 用户 ID。 */
    private Long id;

    /** 登录用户名。 */
    private String username;

    /** 昵称。 */
    private String nickname;

    /** 头像 URL。 */
    private String avatar;

    /** 角色编码列表，day03 接入 RBAC 后填充。 */
    private List<String> roles;

    /** 权限编码列表，day03 接入 RBAC 后填充。 */
    private List<String> permissions;
}
