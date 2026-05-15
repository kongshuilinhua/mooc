package com.elysia.mooc.event.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** 管理端事件接口真实 Spring 上下文和数据库映射测试。 */
@SpringBootTest(properties = "mooc.event.message-consumer-auto-startup=false")
@AutoConfigureMockMvc
class EventAdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 查询发布日志必须走真实 Mapper 和数据库表，避免只测 Mock 后漏掉运行库 SQL 问题。
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void listPublishLogsShouldWorkWithRealDatabaseMapping() throws Exception {
        mockMvc.perform(get("/api/admin/events/publish-logs")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total", notNullValue()))
                .andExpect(jsonPath("$.data.totalPage", notNullValue()))
                .andExpect(jsonPath("$.data.list", notNullValue()));
    }

    /**
     * 查询消费日志必须验证 event_consume_log 字段映射，防止真实 HTTP 才暴露 500。
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void listConsumeLogsShouldWorkWithRealDatabaseMapping() throws Exception {
        mockMvc.perform(get("/api/admin/events/consume-logs")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total", notNullValue()))
                .andExpect(jsonPath("$.data.totalPage", notNullValue()))
                .andExpect(jsonPath("$.data.list", notNullValue()));
    }

    /**
     * 不存在事件应由 BizException 转成中文业务错误，不能落到系统 500。
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void retryMissingEventShouldReturnChineseBusinessError() throws Exception {
        mockMvc.perform(post("/api/admin/events/evt-integration-missing/retry"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(11101))
                .andExpect(jsonPath("$.message").value("事件不存在"));
    }
}
