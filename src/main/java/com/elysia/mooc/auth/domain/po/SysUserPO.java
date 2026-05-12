package com.elysia.mooc.auth.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.common.enums.EnableStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户持久化对象，对应 sys_user 表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user")
public class SysUserPO {

    /** 未删除。 */
    public static final int DELETED_NO = 0;

    /** 用户 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名。 */
    private String username;

    /** BCrypt 密码密文。 */
    private String passwordHash;

    /** 昵称。 */
    private String nickname;

    /** 头像 URL。 */
    private String avatar;

    /** 邮箱。 */
    private String email;

    /** 手机号。 */
    private String phone;

    /** 状态：0 禁用，1 启用。 */
    private EnableStatus status;

    /** 最近登录时间。 */
    private LocalDateTime lastLoginTime;

    /** 最近登录 IP。 */
    private String lastLoginIp;

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

    /** 逻辑删除标记：0 正常，1 删除。 */
    private Integer deleted;

    /**
     * 创建注册用户 PO，集中设置新增用户的默认状态。
     */
    public static SysUserPO createRegisteredUser(
            String username,
            String passwordHash,
            String nickname,
            String email,
            String phone) {
        return SysUserPO.builder()
                .username(username)
                .passwordHash(passwordHash)
                .nickname(nickname)
                .email(email)
                .phone(phone)
                .status(EnableStatus.ENABLED)
                .deleted(DELETED_NO)
                .build();
    }

    /**
     * 记录用户最近一次登录信息。
     */
    public void recordLogin(String clientIp) {
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = clientIp;
    }

    /**
     * 判断账号是否可用。
     */
    public boolean isAvailable() {
        return DELETED_NO == this.deleted && EnableStatus.ENABLED == this.status;
    }
}
