package com.elysia.mooc.rbac.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户角色关系实体，映射 sys_user_role 表。
 * 记录用户被分配了哪些角色，多对多关联。
 */
@Data
@TableName("sys_user_role")
public class SysUserRolePO {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID，关联 sys_user 表 */
    private Long userId;

    /** 角色ID，关联 sys_role 表 */
    private Long roleId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
