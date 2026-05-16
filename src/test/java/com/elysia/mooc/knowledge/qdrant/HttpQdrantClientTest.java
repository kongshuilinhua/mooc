package com.elysia.mooc.knowledge.qdrant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** Qdrant REST 客户端请求格式测试。 */
class HttpQdrantClientTest {

    @Test
    void upsertShouldUseNumericPointIdAndBusinessPayload() {
        QdrantProperties properties = new QdrantProperties();
        properties.setUrl("http://qdrant.test");
        properties.setCollection("mooc_knowledge");
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.getUrl());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpQdrantClient client = new HttpQdrantClient(properties, new ObjectMapper(), builder.build());
        server.expect(requestTo("http://qdrant.test/collections/mooc_knowledge"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://qdrant.test/collections/mooc_knowledge/points?wait=true"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(jsonPath("$.points[0].id").value(12201))
                .andExpect(jsonPath("$.points[0].payload.kbId").value(12001))
                .andExpect(jsonPath("$.points[0].payload.segmentId").value(12201))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        String vectorId = client.upsertPoint(
                "12201",
                Collections.nCopies(1024, 0.1F),
                QdrantPointPayload.builder()
                        .kbId(12001L)
                        .documentId(12101L)
                        .segmentId(12201L)
                        .title("Embedding")
                        .content("切片内容")
                        .build());

        assertThat(vectorId).isEqualTo("12201");
        server.verify();
    }

    @Test
    void searchShouldParsePayload() {
        QdrantProperties properties = new QdrantProperties();
        properties.setUrl("http://qdrant.test");
        properties.setCollection("mooc_knowledge");
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.getUrl());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpQdrantClient client = new HttpQdrantClient(properties, new ObjectMapper(), builder.build());
        server.expect(requestTo("http://qdrant.test/collections/mooc_knowledge"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://qdrant.test/collections/mooc_knowledge/points/search"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.filter.must[0].match.value").value(12001))
                .andRespond(withSuccess("""
                        {
                          "result": [
                            {
                              "id": 12201,
                              "score": 0.91,
                              "payload": {
                                "kbId": 12001,
                                "documentId": 12101,
                                "segmentId": 12201,
                                "title": "Embedding",
                                "content": "切片内容"
                              }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<VectorSearchResult> results = client.search(Collections.nCopies(1024, 0.2F), 12001L, 5);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getVectorId()).isEqualTo("12201");
        assertThat(results.get(0).getPayload().getSegmentId()).isEqualTo(12201L);
        server.verify();
    }

    @Test
    void deleteShouldSkipLegacyPlaceholderPointId() {
        QdrantProperties properties = new QdrantProperties();
        properties.setUrl("http://qdrant.test");
        properties.setCollection("mooc_knowledge");
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.getUrl());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpQdrantClient client = new HttpQdrantClient(properties, new ObjectMapper(), builder.build());

        client.deletePoint("qdrant-12204");

        server.verify();
    }
}
