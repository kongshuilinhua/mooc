package com.elysia.mooc.statistics.controller;

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

/** 管理端统计接口真实安全链路测试。 */
@SpringBootTest(properties = {
        "mooc.event.message-consumer-auto-startup=false",
        "mooc.qdrant.auto-initialize=false"
})
@AutoConfigureMockMvc
class AdminDataSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 学生已登录但不是管理员，访问后台统计必须返回 HTTP 403。
     */
    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void overviewShouldReturn403WhenStudentAuthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/data/overview"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.message()));
    }

    /**
     * 匿名访问后台统计必须返回 HTTP 401。
     */
    @Test
    void dailyShouldReturn401WhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/admin/data/daily"))
                .andExpect(status().isUnauthorized());
    }
}
