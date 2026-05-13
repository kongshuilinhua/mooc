package com.elysia.mooc.course.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.validate.ParamChecker;
import com.elysia.mooc.course.domain.dto.CoursePageQuery;
import com.elysia.mooc.course.domain.dto.CreateCourseRequest;
import com.elysia.mooc.course.domain.dto.UpdateCourseRequest;
import com.elysia.mooc.course.domain.vo.CourseCardVO;
import com.elysia.mooc.course.domain.vo.CourseDetailVO;
import com.elysia.mooc.course.domain.vo.CourseMutationVO;
import com.elysia.mooc.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 课程基础信息接口。 */
@Tag(name = "课程基础信息")
@Validated
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 分页查询课程。
     *
     * @param query 查询条件
     * @return 课程分页
     */
    @Operation(summary = "分页查询课程")
    @GetMapping
    public ApiResult<PageResult<CourseCardVO>> listCourses(@Valid CoursePageQuery query) {
        return ApiResult.ok(courseService.listCourses(query));
    }

    /**
     * 查询课程详情。
     *
     * @param courseId 课程 ID
     * @return 课程详情
     */
    @Operation(summary = "查询课程详情")
    @GetMapping("/{courseId}")
    public ApiResult<CourseDetailVO> getCourseDetail(@PathVariable Long courseId) {
        return ApiResult.ok(courseService.getCourseDetail(courseId));
    }

    /**
     * 创建课程。
     *
     * @param request 创建课程请求
     * @return 课程变更结果
     */
    @Operation(summary = "创建课程")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ParamChecker
    public ApiResult<CourseMutationVO> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        return ApiResult.ok(courseService.createCourse(request));
    }

    /**
     * 修改课程。
     *
     * @param courseId 课程 ID
     * @param request  修改课程请求
     * @return 课程变更结果
     */
    @Operation(summary = "修改课程")
    @PutMapping("/{courseId}")
    @PreAuthorize("isAuthenticated()")
    @ParamChecker
    public ApiResult<CourseMutationVO> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody UpdateCourseRequest request) {
        return ApiResult.ok(courseService.updateCourse(courseId, request));
    }
}
