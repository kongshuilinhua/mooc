package com.elysia.mooc.knowledge.embedding;

import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** 阿里云百炼 OpenAI 兼容 Embedding 客户端。 */
@Slf4j
@Component
public class BailianEmbeddingClient implements EmbeddingClient {

    private final EmbeddingProperties properties;
    private final RestClient restClient;

    public BailianEmbeddingClient(EmbeddingProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));
        requestFactory.setReadTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));
        this.restClient = RestClient.builder()
                .baseUrl(trimTrailingSlash(properties.getBaseUrl()))
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * 调用百炼兼容接口生成向量。
     *
     * @param request 向量化请求
     * @return 向量化结果
     */
    @Override
    public EmbeddingResult embed(EmbeddingRequest request) {
        String apiKey = resolveApiKey();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", request.model());
        body.put("input", request.input());
        body.put("dimensions", request.dimensions());

        try {
            Map<?, ?> response = restClient.post()
                    .uri("/embeddings")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            return parseResponse(response, request.model());
        } catch (RestClientException ex) {
            log.warn("调用百炼向量模型失败，provider={}，model={}", properties.getProvider(), request.model(), ex);
            throw new BizException(CommonErrorCode.EXTERNAL_SERVICE_ERROR, "向量模型调用失败，请稍后重试");
        }
    }

    private EmbeddingResult parseResponse(Map<?, ?> response, String fallbackModel) {
        if (response == null || !(response.get("data") instanceof List<?> data) || data.isEmpty()) {
            throw new BizException(CommonErrorCode.EXTERNAL_SERVICE_ERROR, "向量模型返回结果为空");
        }
        Object first = data.get(0);
        if (!(first instanceof Map<?, ?> item) || !(item.get("embedding") instanceof List<?> embedding)) {
            throw new BizException(CommonErrorCode.EXTERNAL_SERVICE_ERROR, "向量模型返回格式不正确");
        }
        List<Float> vector = new ArrayList<>(embedding.size());
        for (Object value : embedding) {
            if (!(value instanceof Number number)) {
                throw new BizException(CommonErrorCode.EXTERNAL_SERVICE_ERROR, "向量模型返回了非法向量值");
            }
            vector.add(number.floatValue());
        }
        Object model = response.get("model");
        return new EmbeddingResult(vector, model == null ? fallbackModel : String.valueOf(model), vector.size());
    }

    private String resolveApiKey() {
        String apiKey = System.getenv(properties.getApiKeyEnv());
        if (!StringUtils.hasText(apiKey)) {
            throw new BizException(CommonErrorCode.EXTERNAL_SERVICE_ERROR,
                    "未配置" + properties.getApiKeyEnv() + "，无法生成向量");
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
