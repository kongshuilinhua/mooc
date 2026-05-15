package com.elysia.mooc.knowledge.controller;

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

/** 知识库管理接口的真实安全链路测试。 */
@SpringBootTest(properties = "mooc.event.message-consumer-auto-startup=false")
@AutoConfigureMockMvc
class KnowledgeBaseControllerSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 学生已登录但没有知识库管理权限，应返回 HTTP 403，不能落入全局系统异常 500。
     */
    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void listKnowledgeBasesShouldReturn403WhenStudentAuthenticated() throws Exception {
        mockMvc.perform(get("/api/ai/knowledge-bases")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.message()));
    }

    /**
     * 文档管理列表同样属于知识库管理权限边界，学生访问必须按合同返回 403。
     */
    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void listDocumentsShouldReturn403WhenStudentAuthenticated() throws Exception {
        mockMvc.perform(get("/api/ai/documents")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.message()));
    }
}
