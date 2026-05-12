package com.elysia.mooc.rbac.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 角色权限关系实体，映射 sys_role_permission 表。
 * 记录每个角色拥有哪些权限点，多对多关联。
 */
@Data
@TableName("sys_role_permission")
public class SysRolePermissionPO {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色ID，关联 sys_role 表 */
    private Long roleId;

    /** 权限ID，关联 sys_permission 表 */
    private Long permissionId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
