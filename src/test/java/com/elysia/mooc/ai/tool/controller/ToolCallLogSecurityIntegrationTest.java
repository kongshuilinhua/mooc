package com.elysia.mooc.ai.tool.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.error.CommonErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** Tool 日志管理接口真实安全链路测试。 */
@SpringBootTest(properties = {
        "mooc.event.message-consumer-auto-startup=false",
        "mooc.qdrant.auto-initialize=false"
})
@AutoConfigureMockMvc
class ToolCallLogSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 学生不能访问管理端 Tool 调用日志。
     */
    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void listToolLogsShouldReturn403WhenStudentAuthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/ai/tool-logs")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.message()));
    }
}
