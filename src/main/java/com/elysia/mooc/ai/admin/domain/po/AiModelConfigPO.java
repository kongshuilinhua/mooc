package com.elysia.mooc.ai.admin.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.ai.admin.domain.enums.AiModelScene;
import com.elysia.mooc.common.enums.EnableStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** AI 模型配置实体，映射 ai_model_config 表。 */
@Data
@TableName("ai_model_config")
public class AiModelConfigPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模型供应商。 */
    private String provider;

    /** 模型名称。 */
    private String modelName;

    /** 使用场景。 */
    private AiModelScene scene;

    /** OpenAI 兼容接口地址。 */
    private String baseUrl;

    /** API Key 环境变量引用，不保存真实密钥。 */
    private String apiKeyRef;

    /** 温度参数。 */
    private BigDecimal temperature;

    /** 默认召回数量。 */
    private Integer topK;

    /** 是否启用。 */
    private EnableStatus enabled;

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
