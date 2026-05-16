package com.elysia.mooc.ai.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.error.CommonErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** AI 管理后台真实安全链路测试。 */
@SpringBootTest(properties = {
        "mooc.event.message-consumer-auto-startup=false",
        "mooc.qdrant.auto-initialize=false"
})
@AutoConfigureMockMvc
class AiAdminSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 学生不能访问 AI 管理后台查询接口，必须返回真实 HTTP 403。
     */
    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void modelConfigsShouldReturn403WhenStudentAuthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/ai/model-configs")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.message()));
    }

    /**
     * 学生不能访问文档状态统计接口。
     */
    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void documentStatusShouldReturn403WhenStudentAuthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/ai/documents/status"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.message()));
    }

    /**
     * 学生不能访问 AI 调用统计接口。
     */
    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void usageShouldReturn403WhenStudentAuthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/ai/usage"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.message()));
    }

    /**
     * day19 合同要求只有管理员可以修改模型配置，不能依赖未落库的新权限点放行。
     */
    @Test
    @WithMockUser(username = "viewer", authorities = "ai:model-config:manage")
    void updateModelConfigShouldReturn403WhenOnlyHasUnseededManagePermission() throws Exception {
        mockMvc.perform(put("/api/admin/ai/model-configs/19001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":1}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.message()));
    }
}
