package com.elysia.mooc.knowledge.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
@SpringBootTest(properties = {
        "mooc.event.message-consumer-auto-startup=false",
        "mooc.qdrant.auto-initialize=false"
})
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

    /**
     * day13 解析入口挂在管理端路径下，学生访问必须返回真实 HTTP 403。
     */
    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void parseDocumentShouldReturn403WhenStudentAuthenticated() throws Exception {
        mockMvc.perform(post("/api/admin/ai/documents/12101/parse"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.message()));
    }

    /**
     * day14 向量检索调试属于管理端接口，学生访问必须返回 HTTP 403。
     */
    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void vectorSearchShouldReturn403WhenStudentAuthenticated() throws Exception {
        mockMvc.perform(post("/api/admin/ai/vector-search")
                        .contentType("application/json")
                        .content("{\"query\":\"课程怎么学习\",\"topK\":1}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.message()));
    }

    /**
     * 切片列表也属于知识库管理能力，学生不能查看后台切片内容。
     */
    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void listSegmentsShouldReturn403WhenStudentAuthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/ai/documents/12101/segments")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.message()));
    }

    /**
     * day13 合同允许拥有 ai:kb:manage 权限的非 ADMIN 账号操作解析接口。
     */
    @Test
    @WithMockUser(username = "kb-manager", authorities = "ai:kb:manage")
    void parseDocumentShouldEnterServiceWhenUserHasKnowledgeManagePermission() throws Exception {
        mockMvc.perform(post("/api/admin/ai/documents/12101/parse"))
                .andExpect(status().isUnauthorized());
    }
}
