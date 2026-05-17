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

/** 阶段一生产化巡检接口安全链路测试。 */
@SpringBootTest(properties = {
        "mooc.event.message-consumer-auto-startup=false",
        "mooc.qdrant.auto-initialize=false"
})
@AutoConfigureMockMvc
class OpsProductionCheckSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void stageOneCheckShouldReturn401WhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/admin/ops/stage-one-check"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void stageOneCheckShouldReturn403WhenStudentAuthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/ops/stage-one-check"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.message()));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void stageOneCheckShouldReachControllerWhenAdminAuthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/ops/stage-one-check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.stage").value("day01-day23 阶段一"))
                .andExpect(jsonPath("$.data.totalCount").isNumber());
    }
}
