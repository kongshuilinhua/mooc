package com.elysia.mooc.knowledge.qdrant;

import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** 基于 Qdrant REST API 的客户端实现。 */
@Slf4j
@Component
public class HttpQdrantClient implements QdrantClient {

    private final QdrantProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Autowired
    public HttpQdrantClient(QdrantProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));
        requestFactory.setReadTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));
        this.restClient = RestClient.builder()
                .baseUrl(trimTrailingSlash(properties.getUrl()))
                .requestFactory(requestFactory)
                .build();
    }

    HttpQdrantClient(QdrantProperties properties, ObjectMapper objectMapper, RestClient restClient) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
    }

    /**
     * 确认 Qdrant Collection 存在。
     */
    @Override
    public void ensureCollection() {
        try {
            restClient.get()
                    .uri("/collections/{collection}", properties.getCollection())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                createCollection();
                return;
            }
            throw qdrantException("检查 Qdrant Collection 失败", ex);
        } catch (RestClientException ex) {
            throw qdrantException("检查 Qdrant Collection 失败", ex);
        }
    }

    /**
     * 写入或覆盖 Qdrant point。
     *
     * @param pointId point ID
     * @param vector 向量
     * @param payload 业务 payload
     * @return point ID
     */
    @Override
    public String upsertPoint(String pointId, List<Float> vector, QdrantPointPayload payload) {
        ensureCollection();
        Map<String, Object> point = new LinkedHashMap<>();
        point.put("id", toQdrantPointId(pointId));
        point.put("vector", vector);
        point.put("payload", objectMapper.convertValue(payload, new TypeReference<Map<String, Object>>() {
        }));

        Map<String, Object> body = Map.of("points", List.of(point));
        try {
            restClient.put()
                    .uri("/collections/{collection}/points?wait=true", properties.getCollection())
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            return pointId;
        } catch (RestClientException ex) {
            throw qdrantException("写入 Qdrant 向量失败", ex);
        }
    }

    /**
     * 删除 Qdrant point。
     *
     * @param pointId point ID
     */
    @Override
    public void deletePoint(String pointId) {
        if (!StringUtils.hasText(pointId)) {
            return;
        }
        Object qdrantPointId = toDeletableQdrantPointId(pointId);
        if (qdrantPointId == null) {
            log.info("跳过历史占位 Qdrant point 删除，pointId={}", pointId);
            return;
        }
        try {
            restClient.post()
                    .uri("/collections/{collection}/points/delete?wait=true", properties.getCollection())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("points", List.of(qdrantPointId)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return;
            }
            throw qdrantException("删除旧 Qdrant 向量失败", ex);
        } catch (RestClientException ex) {
            throw qdrantException("删除旧 Qdrant 向量失败", ex);
        }
    }

    /**
     * 调用 Qdrant 相似度检索。
     *
     * @param vector 查询向量
     * @param knowledgeBaseId 知识库过滤条件
     * @param topK 返回数量
     * @return 检索命中
     */
    @Override
    public List<VectorSearchResult> search(List<Float> vector, Long knowledgeBaseId, int topK) {
        ensureCollection();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("vector", vector);
        body.put("limit", topK);
        body.put("with_payload", true);
        if (knowledgeBaseId != null) {
            body.put("filter", Map.of(
                    "must", List.of(Map.of("key", "kbId", "match", Map.of("value", knowledgeBaseId)))));
        }
        try {
            Map<?, ?> response = restClient.post()
                    .uri("/collections/{collection}/points/search", properties.getCollection())
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            return parseSearchResponse(response);
        } catch (RestClientException ex) {
            throw qdrantException("Qdrant 向量检索失败", ex);
        }
    }

    private void createCollection() {
        Map<String, Object> body = Map.of(
                "vectors", Map.of(
                        "size", properties.getVectorSize(),
                        "distance", properties.getDistance()));
        try {
            restClient.put()
                    .uri("/collections/{collection}", properties.getCollection())
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw qdrantException("创建 Qdrant Collection 失败", ex);
        }
    }

    private List<VectorSearchResult> parseSearchResponse(Map<?, ?> response) {
        if (response == null || !(response.get("result") instanceof List<?> hits)) {
            return List.of();
        }
        List<VectorSearchResult> results = new ArrayList<>(hits.size());
        for (Object hit : hits) {
            if (!(hit instanceof Map<?, ?> item)) {
                continue;
            }
            QdrantPointPayload payload = objectMapper.convertValue(item.get("payload"), QdrantPointPayload.class);
            Object id = item.get("id");
            Object score = item.get("score");
            results.add(VectorSearchResult.builder()
                    .vectorId(id == null ? null : String.valueOf(id))
                    .score(score instanceof Number number ? number.doubleValue() : null)
                    .payload(payload)
                    .build());
        }
        return results;
    }

    private BizException qdrantException(String message, Exception ex) {
        log.warn("{}，collection={}", message, properties.getCollection(), ex);
        return new BizException(CommonErrorCode.EXTERNAL_SERVICE_ERROR, message + "，请稍后重试");
    }

    private Object toQdrantPointId(String pointId) {
        try {
            return Long.parseLong(pointId);
        } catch (NumberFormatException ex) {
            return pointId;
        }
    }

    private Object toDeletableQdrantPointId(String pointId) {
        Object qdrantPointId = toQdrantPointId(pointId);
        if (!(qdrantPointId instanceof String value)) {
            return qdrantPointId;
        }
        return value.startsWith("qdrant-") ? null : value;
    }

    private String trimTrailingSlash(String url) {
        String value = StringUtils.hasText(url) ? url.trim() : "";
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
