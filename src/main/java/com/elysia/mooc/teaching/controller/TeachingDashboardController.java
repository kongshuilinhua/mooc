package com.elysia.mooc.teaching.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.teaching.domain.dto.TeacherDashboardQuery;
import com.elysia.mooc.teaching.domain.dto.TeacherStudentProgressQuery;
import com.elysia.mooc.teaching.domain.vo.TeacherCourseAnalysisVO;
import com.elysia.mooc.teaching.domain.vo.TeacherDashboardOverviewVO;
import com.elysia.mooc.teaching.domain.vo.TeacherStudentProgressVO;
import com.elysia.mooc.teaching.service.TeachingDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 教师数据看板与课程分析接口。 */
@Tag(name = "教师数据看板")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teacher/dashboard")
public class TeachingDashboardController {

    private final TeachingDashboardService teachingDashboardService;

    /**
     * 查询教师看板总览。
     * @param query 日期范围查询条件
     * @return 教师看板总览
     */
    @Operation(summary = "查询教师看板总览")
    @GetMapping("/overview")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResult<TeacherDashboardOverviewVO> getOverview(@Valid TeacherDashboardQuery query) {
        return ApiResult.ok(teachingDashboardService.getOverview(query));
    }

    /**
     * 查询单门课程分析。
     * @param courseId 课程 ID
     * @param query 日期范围查询条件
     * @return 课程分析数据
     */
    @Operation(summary = "查询课程分析")
    @GetMapping("/courses/{courseId}/analysis")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResult<TeacherCourseAnalysisVO> getCourseAnalysis(
            @PathVariable Long courseId,
            @Valid TeacherDashboardQuery query) {
        return ApiResult.ok(teachingDashboardService.getCourseAnalysis(courseId, query));
    }

    /**
     * 分页查询课程学员进度。
     * @param courseId 课程 ID
     * @param query 学员进度筛选和分页参数
     * @return 学员进度分页结果
     */
    @Operation(summary = "查询课程学员进度")
    @GetMapping("/courses/{courseId}/students")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResult<PageResult<TeacherStudentProgressVO>> listCourseStudents(
            @PathVariable Long courseId,
            @Valid TeacherStudentProgressQuery query) {
        return ApiResult.ok(teachingDashboardService.listCourseStudents(courseId, query));
    }
}
