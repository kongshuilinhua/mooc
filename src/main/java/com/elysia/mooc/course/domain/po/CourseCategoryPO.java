package com.elysia.mooc.course.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.common.enums.EnableStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 课程分类实体，映射 course_category 表。 */
@Data
@TableName("course_category")
public class CourseCategoryPO {

    /** 分类 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父分类 ID，根节点为 0。 */
    private Long parentId;

    /** 分类名称。 */
    private String name;

    /** 分类编码。 */
    private String code;

    /** 分类层级。 */
    private Integer level;

    /** 排序值。 */
    private Integer sort;

    /** 启停状态。 */
    private EnableStatus status;

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

    /** 逻辑删除标记。 */
    @TableLogic
    private Integer deleted;
}
