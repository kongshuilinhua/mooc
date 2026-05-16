package com.elysia.mooc.ai.model;

import com.elysia.mooc.ai.chat.constants.AiChatErrorCode;
import com.elysia.mooc.common.exception.BizException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** 阿里云百炼 OpenAI 兼容聊天客户端。 */
@Slf4j
@Component
public class BailianChatClient implements AiChatClient {

    private final AiChatProperties properties;
    private final RestClient restClient;

    @Autowired
    public BailianChatClient(AiChatProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));
        requestFactory.setReadTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));
        this.restClient = RestClient.builder()
                .baseUrl(trimTrailingSlash(properties.getBaseUrl()))
                .requestFactory(requestFactory)
                .build();
    }

    BailianChatClient(AiChatProperties properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    /**
     * 调用百炼兼容聊天接口。
     *
     * @param request 聊天模型请求
     * @return 模型回复
     */
    @Override
    public ChatCompletionResult complete(ChatCompletionRequest request) {
        String apiKey = resolveApiKey();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", request.model());
        body.put("messages", request.messages().stream()
                .map(message -> Map.of("role", message.role(), "content", message.content()))
                .toList());

        try {
            Map<?, ?> response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            return parseResponse(response, request.model());
        } catch (RestClientException ex) {
            log.warn("调用百炼聊天模型失败，provider={}，model={}", properties.getProvider(), request.model(), ex);
            throw new BizException(AiChatErrorCode.AI_CHAT_MODEL_FAILED);
        }
    }

    private ChatCompletionResult parseResponse(Map<?, ?> response, String fallbackModel) {
        if (response == null || !(response.get("choices") instanceof List<?> choices) || choices.isEmpty()) {
            throw new BizException(AiChatErrorCode.AI_CHAT_MODEL_FAILED, "AI 模型返回结果为空");
        }
        Object first = choices.get(0);
        if (!(first instanceof Map<?, ?> item) || !(item.get("message") instanceof Map<?, ?> message)) {
            throw new BizException(AiChatErrorCode.AI_CHAT_MODEL_FAILED, "AI 模型返回格式不正确");
        }
        Object content = message.get("content");
        if (!StringUtils.hasText(content == null ? null : String.valueOf(content))) {
            throw new BizException(AiChatErrorCode.AI_CHAT_MODEL_FAILED, "AI 模型返回内容为空");
        }
        Map<?, ?> usage = response.get("usage") instanceof Map<?, ?> usageMap ? usageMap : Map.of();
        Object model = response.get("model");
        Object finishReason = item.get("finish_reason");
        return new ChatCompletionResult(
                String.valueOf(content),
                model == null ? fallbackModel : String.valueOf(model),
                toInteger(usage.get("prompt_tokens")),
                toInteger(usage.get("completion_tokens")),
                toInteger(usage.get("total_tokens")),
                finishReason == null ? null : String.valueOf(finishReason));
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null && StringUtils.hasText(String.valueOf(value))) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String resolveApiKey() {
        String apiKey = System.getenv(properties.getApiKeyEnv());
        if (!StringUtils.hasText(apiKey)) {
            throw new BizException(AiChatErrorCode.AI_CHAT_MODEL_CONFIG_MISSING,
                    "未配置" + properties.getApiKeyEnv() + "，无法调用聊天模型");
        }
        return apiKey.trim();
    }

    private String trimTrailingSlash(String url) {
        String value = StringUtils.hasText(url) ? url.trim() : "";
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
