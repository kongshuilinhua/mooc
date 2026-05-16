package com.elysia.mooc.ai.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 普通聊天模型配置，和 Embedding 模型配置分离。 */
@Data
@Component
@ConfigurationProperties(prefix = "mooc.ai.chat")
public class AiChatProperties {

    /** 模型供应商，当前默认使用百炼兼容 OpenAI 接口。 */
    private String provider = "bailian";

    /** OpenAI 兼容接口地址。 */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    /** API Key 所在环境变量名称。 */
    private String apiKeyEnv = "DASHSCOPE_API_KEY";

    /** 普通聊天模型名称。 */
    private String model = "qwen-plus";

    /** 外部模型调用超时时间，单位秒。 */
    private int timeoutSeconds = 60;

    /** 最近历史消息窗口大小。 */
    private int historyLimit = 10;

    /** 系统提示词，集中在配置中便于后续按场景覆盖。 */
    private String systemPrompt = "你是 MOOC 学习平台的 AI 助手，请用中文给出准确、简洁、适合学习场景的回答。";
}
