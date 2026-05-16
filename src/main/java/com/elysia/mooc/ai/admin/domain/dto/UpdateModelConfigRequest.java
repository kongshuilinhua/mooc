package com.elysia.mooc.ai.admin.domain.dto;

import com.elysia.mooc.common.enums.EnableStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

/** 修改 AI 模型配置请求。 */
@Data
public class UpdateModelConfigRequest {

    /** 模型供应商，例如 BAILIAN。 */
    @Size(max = 64, message = "模型供应商长度不能超过64")
    private String provider;

    /** 模型名称。 */
    @Size(max = 128, message = "模型名称长度不能超过128")
    private String modelName;

    /** OpenAI 兼容接口地址。 */
    @Size(max = 500, message = "模型接口地址长度不能超过500")
    private String baseUrl;

    /** API Key 环境变量引用。 */
    @Size(max = 128, message = "API Key 引用长度不能超过128")
    private String apiKeyRef;

    /** 温度参数。 */
    @DecimalMin(value = "0.00", message = "温度参数不能小于0")
    @DecimalMax(value = "2.00", message = "温度参数不能大于2")
    private BigDecimal temperature;

    /** 默认召回数量。 */
    @Min(value = 1, message = "TopK不能小于1")
    @Max(value = 100, message = "TopK不能大于100")
    private Integer topK;

    /** 启停状态，支持 1/0 或 ENABLED/DISABLED。 */
    private EnableStatus enabled;

    /** 兼容只传布尔值的前端表单。 */
    private Boolean enable;
}
