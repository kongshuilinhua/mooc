package com.elysia.mooc.course.domain.vo;

import com.elysia.mooc.common.enums.EnableStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/** 课程分类树视图对象。 */
@Data
public class CourseCategoryVO {

    /** 分类 ID。 */
    private Long id;

    /** 父分类 ID。 */
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

    /** 子分类列表。 */
    private List<CourseCategoryVO> children = new ArrayList<>();
}
