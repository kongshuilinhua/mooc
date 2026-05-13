package com.elysia.mooc.course.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.course.domain.dto.CourseTagQuery;
import com.elysia.mooc.course.domain.vo.CourseTagVO;
import com.elysia.mooc.course.service.CourseTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 课程标签接口。 */
@Tag(name = "课程标签")
@RestController
@RequestMapping("/api/course-tags")
@RequiredArgsConstructor
public class CourseTagController {

    private final CourseTagService courseTagService;

    /**
     * 查询课程标签列表。
     *
     * @param query 查询条件
     * @return 课程标签列表
     */
    @Operation(summary = "查询课程标签列表")
    @GetMapping
    public ApiResult<List<CourseTagVO>> listTags(@Valid CourseTagQuery query) {
        return ApiResult.ok(courseTagService.listTags(query));
    }
}
