package com.elysia.mooc.ops.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.error.CommonErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** 运营配置接口安全链路测试。 */
@SpringBootTest(properties = {
        "mooc.event.message-consumer-auto-startup=false",
        "mooc.qdrant.auto-initialize=false"
})
@AutoConfigureMockMvc
class OpsConfigSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createReviewTaskShouldReturn401WhenAnonymous() throws Exception {
        mockMvc.perform(post("/api/admin/ops-config/reviews")
                        .contentType("application/json")
                        .content("""
                                {"targetType":"COURSE","targetId":"3003","reason":"匿名测试","priority":"MEDIUM"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void createExportJobShouldReturn403WhenStudentAuthenticated() throws Exception {
        mockMvc.perform(post("/api/admin/ops-config/exports")
                        .contentType("application/json")
                        .content("""
                                {"exportType":"COURSE_AUDIT","bizDate":"2026-05-19","format":"XLSX"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()));
    }

    @Test
    @WithMockUser(username = "teacher", roles = "TEACHER")
    void updateConfigItemShouldReturn403WhenTeacherAuthenticated() throws Exception {
        mockMvc.perform(put("/api/admin/ops-config/items/export.downloadExpireHours")
                        .contentType("application/json")
                        .content("""
                                {"value":"24","remark":"教师越权测试"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()));
    }
}
