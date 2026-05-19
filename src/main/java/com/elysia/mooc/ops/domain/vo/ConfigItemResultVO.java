package com.elysia.mooc.ops.domain.vo;

import com.elysia.mooc.ops.domain.enums.OpsConfigStatus;
import com.elysia.mooc.ops.domain.enums.OpsConfigValueType;
import java.time.LocalDateTime;
import lombok.Data;

/** 配置项更新结果。 */
@Data
public class ConfigItemResultVO {

    /** 配置键。 */
    private String configKey;

    /** 配置分组。 */
    private String configGroup;

    /** 配置值。 */
    private String value;

    /** 配置值类型。 */
    private OpsConfigValueType valueType;

    /** 配置状态。 */
    private OpsConfigStatus status;

    /** 变更说明。 */
    private String remark;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
