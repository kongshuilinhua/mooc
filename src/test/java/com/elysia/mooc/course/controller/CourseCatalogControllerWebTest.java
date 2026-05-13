package com.elysia.mooc.course.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.elysia.mooc.course.constants.CourseErrorCode;
import com.elysia.mooc.course.domain.enums.CatalogMutationStatus;
import com.elysia.mooc.course.domain.enums.CatalogNodeType;
import com.elysia.mooc.course.domain.vo.CatalogMutationVO;
import com.elysia.mooc.course.service.CourseCatalogService;
import com.elysia.mooc.course.service.CourseConceptService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** 课程目录控制层 HTTP 合同测试。 */
@ExtendWith(MockitoExtension.class)
class CourseCatalogControllerWebTest {

    @Mock
    private CourseCatalogService courseCatalogService;

    @Mock
    private CourseConceptService courseConceptService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CourseCatalogController controller = new CourseCatalogController(courseCatalogService, courseConceptService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createChapterShouldReturn403WhenStudentForbidden() throws Exception {
        when(courseCatalogService.createChapter(eq(3006L), any()))
                .thenThrow(new BizException(CourseErrorCode.CATALOG_FORBIDDEN));

        mockMvc.perform(post("/api/courses/3006/chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"学生越权章节","summary":"验收","sort":99}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CourseErrorCode.CATALOG_FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(CourseErrorCode.CATALOG_FORBIDDEN.message()));
    }

    @Test
    void createChapterShouldReturn409WhenCourseStatusInvalid() throws Exception {
        when(courseCatalogService.createChapter(eq(3001L), any()))
                .thenThrow(new BizException(CourseErrorCode.CATALOG_STATUS_INVALID));

        mockMvc.perform(post("/api/courses/3001/chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"发布课程章节","summary":"验收","sort":99}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(CourseErrorCode.CATALOG_STATUS_INVALID.code()))
                .andExpect(jsonPath("$.message").value(CourseErrorCode.CATALOG_STATUS_INVALID.message()));
    }

    @Test
    void createChapterShouldReturn200WhenAdminAllowed() throws Exception {
        when(courseCatalogService.createChapter(eq(3006L), any()))
                .thenReturn(CatalogMutationVO.builder()
                        .id(4099L)
                        .courseId(3006L)
                        .type(CatalogNodeType.CHAPTER)
                        .status(CatalogMutationStatus.CREATED)
                        .updateTime(LocalDateTime.now())
                        .build());

        mockMvc.perform(post("/api/courses/3006/chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"管理员章节","summary":"验收","sort":99}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(4099))
                .andExpect(jsonPath("$.data.type").value("CHAPTER"))
                .andExpect(jsonPath("$.data.status").value("CREATED"));
    }
}
