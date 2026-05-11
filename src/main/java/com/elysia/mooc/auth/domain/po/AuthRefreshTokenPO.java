package com.elysia.mooc.auth.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新令牌持久化对象，对应 auth_refresh_token 表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("auth_refresh_token")
public class AuthRefreshTokenPO {

    /** 未撤销。 */
    public static final int REVOKED_NO = 0;

    /** 已撤销。 */
    public static final int REVOKED_YES = 1;

    /** 未删除。 */
    public static final int DELETED_NO = 0;

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** refreshToken 的 SHA-256 摘要。 */
    private String tokenHash;

    /** 设备标识。 */
    private String deviceId;

    /** 客户端类型。 */
    private String clientType;

    /** 过期时间。 */
    private LocalDateTime expireTime;

    /** 是否撤销：0 有效，1 已撤销。 */
    private Integer revoked;

    /** 撤销时间。 */
    private LocalDateTime revokedTime;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;

    /** 创建人 ID。 */
    private Long createBy;

    /** 更新人 ID。 */
    private Long updateBy;

    /** 逻辑删除标记：0 正常，1 删除。 */
    private Integer deleted;

    /**
     * 创建新的刷新令牌 PO，集中设置令牌默认状态。
     */
    public static AuthRefreshTokenPO issue(
            Long userId,
            String tokenHash,
            String deviceId,
            String clientType,
            LocalDateTime expireTime) {
        return AuthRefreshTokenPO.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .deviceId(deviceId)
                .clientType(clientType)
                .expireTime(expireTime)
                .revoked(REVOKED_NO)
                .deleted(DELETED_NO)
                .build();
    }

    /**
     * 撤销刷新令牌。
     */
    public void revoke() {
        this.revoked = REVOKED_YES;
        this.revokedTime = LocalDateTime.now();
    }

    /**
     * 判断刷新令牌当前是否有效。
     */
    public boolean isActive() {
        return DELETED_NO == this.deleted
                && REVOKED_NO == this.revoked
                && this.expireTime != null
                && this.expireTime.isAfter(LocalDateTime.now());
    }
}
