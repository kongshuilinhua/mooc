package com.elysia.mooc.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/** OpenAPI 文档端点冒烟测试，避免依赖升级后运行时才发现 Swagger 不可用。 */
@SpringBootTest(properties = {
        "mooc.event.message-consumer-auto-startup=false",
        "mooc.qdrant.auto-initialize=false"
})
@AutoConfigureMockMvc
class OpenApiSmokeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * OpenAPI 文档属于 day24 生产化巡检项，必须允许匿名读取并正常生成接口 JSON。
     */
    @Test
    void apiDocsShouldReturnOpenApiJson() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("MOOC API"))
                .andExpect(jsonPath("$.paths").exists());
    }
}
