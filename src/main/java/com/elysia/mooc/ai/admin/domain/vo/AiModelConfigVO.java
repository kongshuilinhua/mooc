package com.elysia.mooc.ai.admin.domain.vo;

import com.elysia.mooc.ai.admin.domain.enums.AiModelScene;
import com.elysia.mooc.common.enums.EnableStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** AI 模型配置展示对象。 */
@Data
public class AiModelConfigVO {

    /** 主键 ID。 */
    private Long id;

    /** 模型供应商。 */
    private String provider;

    /** 模型名称。 */
    private String modelName;

    /** 使用场景。 */
    private AiModelScene scene;

    /** OpenAI 兼容接口地址。 */
    private String baseUrl;

    /** API Key 环境变量引用，不包含真实 Key。 */
    private String apiKeyRef;

    /** 是否已配置 API Key 引用。 */
    private Boolean apiKeyConfigured;

    /** 温度参数。 */
    private BigDecimal temperature;

    /** 默认召回数量。 */
    private Integer topK;

    /** 启停状态。 */
    private EnableStatus enabled;

    /** 兼容通用表格状态字段。 */
    private EnableStatus status;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;

    /** 创建人 ID。 */
    private Long createBy;

    /** 更新人 ID。 */
    private Long updateBy;
}
