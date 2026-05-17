package com.elysia.mooc.ops.controller;

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

/** 管理端审计与幂等接口安全链路测试。 */
@SpringBootTest(properties = {
        "mooc.event.message-consumer-auto-startup=false",
        "mooc.qdrant.auto-initialize=false"
})
@AutoConfigureMockMvc
class OpsLogSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void auditLogsShouldReturn403WhenStudentAuthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs?pageNo=1&pageSize=10"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()));
    }

    @Test
    void idempotentRecordsShouldReturn401WhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/admin/idempotent-records?pageNo=1&pageSize=10"))
                .andExpect(status().isUnauthorized());
    }
}
