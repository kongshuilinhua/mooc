package com.elysia.mooc.course.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.course.domain.vo.CourseCategoryVO;
import com.elysia.mooc.course.service.CourseCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 课程分类接口。 */
@Tag(name = "课程分类")
@RestController
@RequestMapping("/api/course-categories")
@RequiredArgsConstructor
public class CourseCategoryController {

    private final CourseCategoryService courseCategoryService;

    /**
     * 查询课程分类树。
     *
     * @return 课程分类树
     */
    @Operation(summary = "查询课程分类树")
    @GetMapping
    public ApiResult<List<CourseCategoryVO>> listCategoryTree() {
        return ApiResult.ok(courseCategoryService.listCategoryTree());
    }
}
