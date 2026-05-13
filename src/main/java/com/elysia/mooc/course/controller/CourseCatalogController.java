package com.elysia.mooc.course.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.validate.ParamChecker;
import com.elysia.mooc.course.domain.dto.CreateChapterRequest;
import com.elysia.mooc.course.domain.dto.CreateConceptRequest;
import com.elysia.mooc.course.domain.dto.CreateSectionRequest;
import com.elysia.mooc.course.domain.dto.UpdateChapterRequest;
import com.elysia.mooc.course.domain.dto.UpdateSectionRequest;
import com.elysia.mooc.course.domain.vo.CatalogMutationVO;
import com.elysia.mooc.course.domain.vo.CourseCatalogVO;
import com.elysia.mooc.course.service.CourseCatalogService;
import com.elysia.mooc.course.service.CourseConceptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 课程章节、小节和知识点接口。 */
@Tag(name = "课程目录")
@Validated
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CourseCatalogController {

    private final CourseCatalogService courseCatalogService;
    private final CourseConceptService courseConceptService;

    /**
     * 查询课程目录树。
     *
     * @param courseId 课程 ID
     * @return 课程目录树
     */
    @Operation(summary = "查询课程目录树")
    @GetMapping("/courses/{courseId}/catalog")
    public ApiResult<CourseCatalogVO> getCourseCatalog(@PathVariable Long courseId) {
        return ApiResult.ok(courseCatalogService.getCourseCatalog(courseId));
    }

    /**
     * 创建章节。
     *
     * @param courseId 课程 ID
     * @param request  创建章节请求
     * @return 目录变更结果
     */
    @Operation(summary = "创建章节")
    @PostMapping("/courses/{courseId}/chapters")
    @PreAuthorize("isAuthenticated()")
    @ParamChecker
    public ApiResult<CatalogMutationVO> createChapter(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateChapterRequest request) {
        return ApiResult.ok(courseCatalogService.createChapter(courseId, request));
    }

    /**
     * 修改章节。
     *
     * @param chapterId 章节 ID
     * @param request   修改章节请求
     * @return 目录变更结果
     */
    @Operation(summary = "修改章节")
    @PutMapping("/chapters/{chapterId}")
    @PreAuthorize("isAuthenticated()")
    @ParamChecker
    public ApiResult<CatalogMutationVO> updateChapter(
            @PathVariable Long chapterId,
            @Valid @RequestBody UpdateChapterRequest request) {
        return ApiResult.ok(courseCatalogService.updateChapter(chapterId, request));
    }

    /**
     * 删除章节。
     *
     * @param chapterId 章节 ID
     * @return 目录变更结果
     */
    @Operation(summary = "删除章节")
    @DeleteMapping("/chapters/{chapterId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<CatalogMutationVO> deleteChapter(@PathVariable Long chapterId) {
        return ApiResult.ok(courseCatalogService.deleteChapter(chapterId));
    }

    /**
     * 创建小节。
     *
     * @param chapterId 章节 ID
     * @param request   创建小节请求
     * @return 目录变更结果
     */
    @Operation(summary = "创建小节")
    @PostMapping("/chapters/{chapterId}/sections")
    @PreAuthorize("isAuthenticated()")
    @ParamChecker
    public ApiResult<CatalogMutationVO> createSection(
            @PathVariable Long chapterId,
            @Valid @RequestBody CreateSectionRequest request) {
        return ApiResult.ok(courseCatalogService.createSection(chapterId, request));
    }

    /**
     * 修改小节。
     *
     * @param sectionId 小节 ID
     * @param request   修改小节请求
     * @return 目录变更结果
     */
    @Operation(summary = "修改小节")
    @PutMapping("/sections/{sectionId}")
    @PreAuthorize("isAuthenticated()")
    @ParamChecker
    public ApiResult<CatalogMutationVO> updateSection(
            @PathVariable Long sectionId,
            @Valid @RequestBody UpdateSectionRequest request) {
        return ApiResult.ok(courseCatalogService.updateSection(sectionId, request));
    }

    /**
     * 删除小节。
     *
     * @param sectionId 小节 ID
     * @return 目录变更结果
     */
    @Operation(summary = "删除小节")
    @DeleteMapping("/sections/{sectionId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<CatalogMutationVO> deleteSection(@PathVariable Long sectionId) {
        return ApiResult.ok(courseCatalogService.deleteSection(sectionId));
    }

    /**
     * 创建知识点。
     *
     * @param courseId 课程 ID
     * @param request  创建知识点请求
     * @return 目录变更结果
     */
    @Operation(summary = "创建知识点")
    @PostMapping("/courses/{courseId}/concepts")
    @PreAuthorize("isAuthenticated()")
    @ParamChecker
    public ApiResult<CatalogMutationVO> createConcept(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateConceptRequest request) {
        return ApiResult.ok(courseConceptService.createConcept(courseId, request));
    }
}
