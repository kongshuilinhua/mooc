package com.elysia.mooc.exam.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

/** 考试模块真实安全链路测试。 */
@SpringBootTest(properties = {
        "mooc.event.message-consumer-auto-startup=false",
        "mooc.qdrant.auto-initialize=false"
})
@AutoConfigureMockMvc
class ExamSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 学生不能创建题目，必须返回真实 HTTP 403。
     */
    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void createQuestionShouldReturn403WhenStudentAuthenticated() throws Exception {
        mockMvc.perform(post("/api/exam/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"courseId":3001,"questionType":"SINGLE","stem":"题干","answerText":"A","options":[{"optionKey":"A","optionText":"正确","correct":true}]}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.message()));
    }

    /**
     * 学生不能创建试卷，必须返回真实 HTTP 403。
     */
    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void createPaperShouldReturn403WhenStudentAuthenticated() throws Exception {
        mockMvc.perform(post("/api/exam/papers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"courseId":3001,"title":"认证基础测验","questionIds":[20001,20002]}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.message()));
    }
}
