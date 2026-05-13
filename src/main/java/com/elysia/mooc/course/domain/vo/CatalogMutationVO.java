package com.elysia.mooc.course.domain.vo;

import com.elysia.mooc.course.domain.enums.CatalogMutationStatus;
import com.elysia.mooc.course.domain.enums.CatalogNodeType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 课程目录变更结果。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogMutationVO {

    /** 变更对象 ID。 */
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 目录节点类型。 */
    private CatalogNodeType type;

    /** 变更状态。 */
    private CatalogMutationStatus status;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
